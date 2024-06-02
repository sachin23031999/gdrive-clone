package com.sachin.gdrive.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sachin.gdrive.R
import com.sachin.gdrive.ui.theme.surfaceDimLight
import com.sachin.gdrive.ui.widget.TextBox

@Composable
fun ListItem(
    @DrawableRes iconId: Int,
    name: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .height(60.dp)
            .fillMaxWidth()
            .clip(CircleShape)
            .background(surfaceDimLight)
            .clickable { onClick() }
    ) {
        Image(
            modifier = Modifier
                .size(54.dp)
                .align(Alignment.CenterVertically)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            painter = painterResource(id = iconId),
            contentDescription = "Folder Icon"
        )
        TextBox(
            text = name,
            fontSize = 18.sp,
            modifier = Modifier.padding(8.dp)
                .align(Alignment.CenterVertically)
        ) {
            onClick()
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun FolderItemPreview() {
    ListItem(iconId = R.drawable.ic_folder, name = "Folder 1", onClick = {})
}