package com.sachin.gdrive.dashboard

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.sachin.gdrive.R
import com.sachin.gdrive.adapter.ItemClickListener
import com.sachin.gdrive.common.Utils
import com.sachin.gdrive.common.getActivity
import com.sachin.gdrive.common.log.logD
import com.sachin.gdrive.common.showToast
import com.sachin.gdrive.model.DriveEntity
import com.sachin.gdrive.provider.DriveServiceProvider
import com.sachin.gdrive.ui.ListItem
import com.sachin.gdrive.ui.widget.ActionButton
import com.sachin.gdrive.ui.widget.ActionMenu
import com.sachin.gdrive.ui.widget.DialogBox
import com.sachin.gdrive.ui.widget.MenuAction
import org.koin.androidx.compose.getViewModel
import java.util.Stack

private var downloadInProgress = false
private val tempDir by lazy {
    Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOWNLOADS
    )
}

@Composable
fun DashboardScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = getViewModel()
) {
    val initialised = remember { mutableStateOf(false) }
    val selectedAction: MutableState<MenuAction?> = remember { mutableStateOf(null) }
    val shouldShowDialog = remember { mutableStateOf(true) }
    val ctx = LocalContext.current
    val folderStack = remember {
        Stack<DriveEntity.Folder>().apply {
            add(
                DriveEntity.Folder(
                    DriveServiceProvider.ROOT_FOLDER_ID,
                    DriveServiceProvider.ROOT_FOLDER_ID.lowercase()
                )
            )
        }
    }

    // Adding root directory to stack.
    val filePickerContract = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.let {
            handlePickedFile(it, ctx, viewModel, folderStack)
        } ?: ctx.showToast("No file selected")
    }

    SetupBackPress(viewModel, folderStack)
    viewModel.init(ctx)
    SetupObservers(viewModel, folderStack, modifier)
    initialised.value = true

    CreateActions(modifier = modifier) { action ->
        selectedAction.value = action
    }

    when (selectedAction.value) {
        MenuAction.ADD_FILE -> {
            filePickerContract.launch(filePickerIntent())
        }

        MenuAction.ADD_FOLDER -> {
            shouldShowDialog.value = true
            DialogBox(
                shouldShow = shouldShowDialog,
                title = "Add Folder",
                onPositiveClick = { folderName ->
                    selectedAction.value = null
                    viewModel.createFolder(folderStack.peek().id, folderName)
                },
                onNegativeClick = {
                    selectedAction.value = null
                })
        }

        else -> {}
    }
}


private fun filePickerIntent() =
    Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
        flags = Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }

private fun handlePickedFile(
    data: Intent,
    ctx: Context,
    viewModel: DashboardViewModel,
    folderStack: Stack<DriveEntity.Folder>
) {
    data.data?.also { uri ->
        logD { "start upload file: $uri" }
        viewModel.startUpload(
            context = ctx,
            parentId = folderStack.peek().id,
            fileName = Utils.getFilename(ctx, uri),
            fileUri = uri
        )
    }
}

@Composable
fun CreateActions(
    modifier: Modifier, onClick: (MenuAction) -> Unit
) {
    val visible = remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        contentAlignment = BottomEnd
    ) {
        if (visible.value) {
            Column(modifier = Modifier.align(BottomEnd)) {
                ActionMenu {
                    visible.value = !visible.value
                    onClick(it)
                }
                ActionButton(
                    resId = R.drawable.ic_close
                ) {
                    visible.value = !visible.value
                }
            }
        } else {
            ActionButton(
                resId = R.drawable.ic_add
            ) {
                visible.value = !visible.value
            }
        }
    }
}

@Composable
private fun SetupObservers(
    viewModel: DashboardViewModel,
    folderStack: Stack<DriveEntity.Folder>,
    modifier: Modifier
) {
    SetupUiStateObserver(viewModel, folderStack) { entities ->
        CreateItemList(modifier, entities, listenClicks(viewModel, folderStack))
    }
}

@Composable
private fun SetupUiStateObserver(
    viewModel: DashboardViewModel,
    folderStack: Stack<DriveEntity.Folder>,
    data: @Composable (List<DriveEntity>) -> Unit
) {
    val ctx = LocalContext.current
    viewModel.uiState.observeAsState().value?.let { state ->
        when (state) {
            is DashboardState.InitSuccess -> {
                fetchFilesAndFolders(LocalContext.current, viewModel, folderStack)
            }

            is DashboardState.InitFailed -> {
                ctx.showToast("Drive service init failed")
            }

            is DashboardState.FetchSuccess -> {
                data(state.entities)
            }

            is DashboardState.FetchFailed -> {
                ctx.showToast(state.error)
            }
        }
    }
}

@Composable
private fun listenClicks(
    viewModel: DashboardViewModel,
    folderStack: Stack<DriveEntity.Folder>
) = object : ItemClickListener {
    val context = LocalContext.current
    override fun onFileClick(file: DriveEntity.File) {
        tempDir?.let {
            if (!downloadInProgress) {
                viewModel.startDownload(it, file)
            } else {
                context.showToast("Download already in progress")
            }
        }
    }

    override fun onFolderClick(folder: DriveEntity.Folder) {
        folderStack.add(folder)
        // menuItemDelete?.isVisible = false
        logD { "folder click: ${folder.name}" }
        fetchFilesAndFolders(context, viewModel, folderStack, folder.id)
    }

    override fun onItemLongClick(item: DriveEntity) {
        //  handleDelete(item)
    }
}

@Composable
private fun CreateItemList(
    modifier: Modifier,
    entities: List<DriveEntity>,
    listener: ItemClickListener
) {
    LazyColumn(modifier = modifier) {
        items(entities) { entity ->
            when (entity) {
                is DriveEntity.Folder -> {
                    ListItem(iconId = R.drawable.ic_folder, name = entity.name) {
                        listener.onFolderClick(entity)
                    }
                }

                is DriveEntity.File -> {
                    ListItem(iconId = R.drawable.ic_file, name = entity.name) {
                        listener.onFileClick(entity)
                    }
                }
            }
        }
    }
}

private fun fetchFilesAndFolders(
    context: Context,
    viewModel: DashboardViewModel,
    folderStack: Stack<DriveEntity.Folder>,
    parentId: String = DriveServiceProvider.ROOT_FOLDER_ID
) {
    logD { "current folder stack: ${folderStack.joinToString("\n")}" }
    viewModel.fetchAll(context, parentId)
}

@Composable
private fun SetupBackPress(
    viewModel: DashboardViewModel,
    folderStack: Stack<DriveEntity.Folder>
) {
    val ctx = LocalContext.current
    BackHandler {
        logD { "current folder id: ${folderStack.peek().id} name: ${folderStack.peek().id}" }
        if (folderStack.peek().id == DriveServiceProvider.ROOT_FOLDER_ID) {
            ctx.getActivity()?.finishAffinity()
        } else {
            folderStack.pop()
            fetchFilesAndFolders(
                context = ctx,
                viewModel = viewModel,
                folderStack = folderStack,
                parentId = folderStack.peek().id
            )
        }
    }
}

@Composable
@Preview(showSystemUi = true)
private fun Preview() {
    /*AddFloatingButton {

    }*/
}