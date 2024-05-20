package com.sachin.gdrive.dashboard

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkManager
import com.sachin.gdrive.R
import com.sachin.gdrive.adapter.FileAdapter
import com.sachin.gdrive.adapter.ItemClickListener
import com.sachin.gdrive.common.Utils
import com.sachin.gdrive.common.handleOnBackPressed
import com.sachin.gdrive.common.log.logD
import com.sachin.gdrive.common.navigateTo
import com.sachin.gdrive.common.showToast
import com.sachin.gdrive.databinding.FragmentDashboardBinding
import com.sachin.gdrive.databinding.LayoutDialogAddBinding
import com.sachin.gdrive.model.DriveEntity
import com.sachin.gdrive.provider.DriveServiceProvider
import org.koin.android.ext.android.inject
import java.util.Stack

class DashboardFragment : Fragment() {
    private val binding by lazy { FragmentDashboardBinding.inflate(layoutInflater) }
    private val fileAdapter: FileAdapter by lazy {
        FileAdapter(requireContext(), itemClickListener)
    }
    private val viewModel: DashboardViewModel by inject()
    private var menuItemDelete: MenuItem? = null
    private var menuItemLogout: MenuItem? = null

    // Adding root directory to stack.
    private val folderStack = Stack<DriveEntity.Folder>().apply {
        add(DriveEntity.Folder(DriveServiceProvider.ROOT_FOLDER_ID, "ROOT"))
    }
    private val itemClickListener = object : ItemClickListener {
        override fun onFileClick(file: DriveEntity.File) {
            menuItemDelete?.isVisible = false
        }

        override fun onFolderClick(folder: DriveEntity.Folder) {
            folderStack.add(folder)
            menuItemDelete?.isVisible = false
            fetchFilesAndFolders(folder.id)
        }

        override fun onItemLongClick(item: DriveEntity) {
            handleDelete(item)
        }
    }

    private val pickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
        flags = Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }

    private val filePickerContract = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.let {
            handlePickedFile(it)
        } ?: showToast("No file selected")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackPress()
        setupObservers()
        addMenu()
        viewModel.init(requireContext())
        setupClicks()
        setupRecyclerView()
        handleSwipeRefresh()
    }

    private fun handleSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            fetchFilesAndFolders(folderStack.peek().id)
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun fetchFilesAndFolders(parentId: String = "root") {
        logD { "current folder stack: ${folderStack.joinToString("\n")}" }
        viewModel.fetchAll(requireContext(), parentId)
    }

    private fun setupClicks() {
        binding.buttonAddFile.setOnClickListener {
            filePickerContract.launch(pickerIntent)
        }
        binding.buttonAddFolder.setOnClickListener {
            showAddDialog { folderName ->
                viewModel.createFolder(folderStack.peek().id, folderName)
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvFiles.apply {
            adapter = fileAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupObservers() {
        setupUiStateObserver()
        setupUploadStateObserver()
        setupCreateFolderObserver()
        setupDeleteObserver()
        setupLogoutObserver()
    }

    private fun setupUiStateObserver() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DashboardState.InitSuccess -> {
                    fetchFilesAndFolders()
                }

                is DashboardState.InitFailed -> {
                    showToast("Drive service init failed")
                }

                is DashboardState.FetchInProgress -> {

                }

                is DashboardState.FetchSuccess -> {
                    fileAdapter.updateEntities(state.entities)
                }

            }
        }
    }

    private fun setupUploadStateObserver() {
        viewModel.uploadState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UploadState.Started -> {
                    showToast("Uploading ${state.fileName}")
                }

                is UploadState.Uploading -> {
                    logD { "progress: ${state.progress}" }
                }

                is UploadState.Uploaded -> {
                    logD { "file uploaded" }
                    showToast("${state.fileName} uploaded")
                    fetchFilesAndFolders(folderStack.peek().id)
                }

                is UploadState.Failed -> {
                    logD { "upload failed" }
                    showToast(state.error)
                }
            }
        }
    }

    private fun setupCreateFolderObserver() {
        viewModel.createFolderState.observe(viewLifecycleOwner) { success ->
            if (success) {
                logD { "Folder created" }
                fetchFilesAndFolders(folderStack.peek().id)
            } else {
                showToast("Failed to create folder")
            }
        }
    }

    private fun setupDeleteObserver() {
        viewModel.deleteState.observe(viewLifecycleOwner) { success ->
            if (success) {
                logD { "Item deleted" }
                fetchFilesAndFolders(folderStack.peek().id)
            } else {
                showToast("Delete failed")
            }
        }
    }

    private fun setupLogoutObserver() {
        viewModel.isLoggedOut.observe(viewLifecycleOwner) { loggedOut ->
            if(loggedOut) {
                menuItemLogout?.isVisible = false
                navigateTo(R.id.signInFragment)
                showToast("logged out")
            } else {
                showToast("logout failed")
            }
        }
    }


    private fun handleDelete(item: DriveEntity) {
        menuItemDelete?.apply {
            isVisible = true
            setOnMenuItemClickListener {
                isVisible = false
                if (item is DriveEntity.File)
                    viewModel.deleteItem(requireContext(), item.id)
                else if (item is DriveEntity.Folder) {
                    viewModel.deleteItem(requireContext(), item.id)
                }
                true
            }
        }
    }

    private fun showAddDialog(onDone: (String) -> Unit) {
        val dialogBinding = LayoutDialogAddBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Create Folder")
            .setView(dialogBinding.root)
            .setPositiveButton("Create") { _, _ ->
                dialogBinding.editText.text.toString().let(onDone)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun setupBackPress() {
        handleOnBackPressed {
            logD { "current folder id: ${folderStack.peek().id} name: ${folderStack.peek().id}" }
            if (folderStack.peek().id == DriveServiceProvider.ROOT_FOLDER_ID) {
                activity?.finishAffinity()
            } else {
                folderStack.pop()
                fetchFilesAndFolders(folderStack.peek().id)
            }

        }
    }

    private fun handlePickedFile(data: Intent) {
        data.data?.also { uri ->
            logD { "start upload file: $uri" }
            viewModel.startUpload(
                context = requireContext(),
                parentId = folderStack.peek().id,
                fileName = Utils.getFilename(requireContext(), uri),
                fileUri = uri
            )
        }
    }

    private fun addMenu() {
        activity?.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_menu, menu)
                menuItemDelete = menu.findItem(R.id.action_delete)
                menuItemLogout = menu.findItem(R.id.action_logout)
                menuItemLogout?.isVisible = true
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.action_delete -> {
                        false
                    }

                    R.id.action_logout -> {
                        viewModel.logout()
                        false
                    }

                    else -> {
                        true
                    }
                }
        })
    }
}

