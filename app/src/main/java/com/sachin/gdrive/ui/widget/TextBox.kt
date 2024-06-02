package com.sachin.gdrive.ui.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

@Composable
fun TextBox(
    text: String,
    fontSize: TextUnit = MaterialTheme.typography.bodyMedium.fontSize,
    modifier: Modifier = Modifier
        .wrapContentWidth()
        .wrapContentHeight(),
    onClick: (() -> Unit) = {}
) {
    Text(
        text = text,
        fontSize = fontSize,
        modifier = modifier
            .wrapContentWidth()
            .wrapContentHeight()
            .clickable { onClick() },
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
}

@Preview
@Composable
private fun TextBoxPreview() {
    TextBox(text = "Hello", onClick = {})
}
