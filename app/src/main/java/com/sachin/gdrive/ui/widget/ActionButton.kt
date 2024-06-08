package com.sachin.gdrive.ui.widget

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    @DrawableRes resId: Int,
    onClick: () -> Unit = {}) {
    FloatingActionButton(
        modifier = modifier
            .padding(8.dp),
        onClick = onClick
    ) {
        Image(painter = painterResource(id = resId), contentDescription = "")
    }
}