package com.sachin.gdrive.ui.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun DialogBox(
    type: DialogType = DialogType.INFO,
    shouldShow: MutableState<Boolean> = mutableStateOf(true),
    title: String,
    desc: String = "",
    positiveText: String = "OK",
    negativeText: String = "Cancel",
    hint: String = "Enter name",
    onPositiveClick: (String) -> Unit,
    onNegativeClick: () -> Unit = {}
) {
    val finalText = remember { mutableStateOf("") }
    if (shouldShow.value) {
        Dialog(
            onDismissRequest = {
                shouldShow.value = false
                onNegativeClick()
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .wrapContentSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextBox(text = title, fontSize = 22.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    when (type) {
                        DialogType.INFO -> {
                            TextBox(text = desc, fontSize = 16.sp)
                        }

                        DialogType.FORM -> {
                            TextField(
                                placeholder = {
                                    Text(text = hint)
                                },
                                maxLines = 1,
                                value = finalText.value,
                                onValueChange = { finalText.value = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        val mod = Modifier
                            .padding(4.dp)
                            .width(100.dp)
                        Button(
                            modifier = mod,
                            onClick = {
                                shouldShow.value = false
                                onNegativeClick()
                            }) {
                            Text(negativeText)
                        }
                        Button(
                            modifier = mod,
                            onClick = {
                                shouldShow.value = false
                                onPositiveClick(finalText.value)
                            }) {
                            Text(positiveText)
                        }
                    }
                }
            }
        }
    }
}

enum class DialogType {
    INFO,
    FORM
}

@Composable
@Preview
private fun Preview() {
    DialogBox(title = "Title",
        desc = "Are you sure?", onPositiveClick = {})
}