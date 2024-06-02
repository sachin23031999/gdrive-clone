package com.sachin.gdrive.ui.widget

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sachin.gdrive.ui.theme.onSurfaceDark
import com.sachin.gdrive.ui.theme.onSurfaceLight

@Composable
fun RoundButton(
    backgroundColor: Color = onSurfaceLight,
    content: @Composable RowScope.() -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.padding(16.dp)
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .clip(CircleShape),
        colors = ButtonDefaults.buttonColors(
            contentColor = onSurfaceDark,
        )
    ) {
        content()
    }
}

@Preview
@Composable
private fun RoundButtonPreview() {
    RoundButton(content = {
        TextBox(text = "Button") {

        }
    }, onClick = { })
}
