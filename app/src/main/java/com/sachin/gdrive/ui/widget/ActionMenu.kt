package com.sachin.gdrive.ui.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sachin.gdrive.R
@Composable
fun ActionMenu(
    modifier: Modifier = Modifier,
    onClick: (MenuAction) -> Unit
) {
    Column(modifier = modifier) {
        ActionButton(
            resId = R.drawable.ic_file_upload) {
            onClick(MenuAction.ADD_FILE)
        }
        ActionButton(
            resId = R.drawable.ic_create_folder) {
            onClick(MenuAction.ADD_FOLDER)
        }
    }
}

enum class MenuAction {
    ADD_FILE,
    ADD_FOLDER
}