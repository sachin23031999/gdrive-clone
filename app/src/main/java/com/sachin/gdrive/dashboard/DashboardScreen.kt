package com.sachin.gdrive.dashboard

import android.content.Context
import android.os.Environment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.sachin.gdrive.R
import com.sachin.gdrive.adapter.ItemClickListener
import com.sachin.gdrive.common.log.logD
import com.sachin.gdrive.common.showToast
import com.sachin.gdrive.model.DriveEntity
import com.sachin.gdrive.provider.DriveServiceProvider
import com.sachin.gdrive.ui.ListItem
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
    modifier: Modifier = Modifier
) {
    val viewModel: DashboardViewModel = getViewModel()
    val ctx = LocalContext.current
    // Adding root directory to stack.
    val folderStack = Stack<DriveEntity.Folder>().apply {
        add(
            DriveEntity.Folder(
                DriveServiceProvider.ROOT_FOLDER_ID,
                DriveServiceProvider.ROOT_FOLDER_ID.lowercase()
            )
        )
    }
    viewModel.init(ctx)
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