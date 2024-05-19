package com.sachin.gdrive.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.sachin.gdrive.adapter.FileAdapter
import com.sachin.gdrive.adapter.ItemClickListener
import com.sachin.gdrive.common.Utils
import com.sachin.gdrive.common.log.logD
import com.sachin.gdrive.common.showToast
import com.sachin.gdrive.databinding.FragmentDashboardBinding
import com.sachin.gdrive.model.DriveEntity
import org.koin.android.ext.android.inject

class DashboardFragment : Fragment() {
    private val binding by lazy { FragmentDashboardBinding.inflate(layoutInflater) }
    private val fileAdapter: FileAdapter by lazy { FileAdapter(itemClickListener) }
    private val viewModel: DashboardViewModel by inject()
    private val itemClickListener = object : ItemClickListener {
        override fun onFileClick(fileName: String) {
            TODO("Not yet implemented")
        }

        override fun onFolderClick(folderName: String, childFiles: List<DriveEntity>) {
            fileAdapter.updateNodes(childFiles)
        }
    }

    private val pickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
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
        viewModel.init(requireContext())
        setupClicks()
        setupRecyclerView()

    }

    private fun setupClicks() {
        binding.buttonAdd.setOnClickListener {
            logD { "add file clicked" }
            filePickerContract.launch(pickerIntent)
        }
    }

    private fun setupRecyclerView() {
        binding.rvFiles.adapter = fileAdapter
    }

    private fun setupObservers() {
        viewModel.uploadState.observe(viewLifecycleOwner) { state ->
            when(state) {
                is UploadState.Uploading -> {
                    logD { "upload progress: ${state.progress}" }
                }
                is UploadState.Uploaded -> {
                    logD { "file uploaded" }
                }
                is UploadState.Failed -> {
                    logD { "upload failed" }
                    showToast(state.error)
                }
            }
        }
    }

    private fun setupBackPress() {

    }

    private fun handlePickedFile(data: Intent) {
        data.data?.also { uri ->
            logD { "start upload file: $uri" }
            viewModel.startUpload(
                context = requireContext(),
                parentFolder = null,
                fileName = Utils.getFilename(requireContext(), uri),
                fileUri = uri
            )
        }
    }
}
