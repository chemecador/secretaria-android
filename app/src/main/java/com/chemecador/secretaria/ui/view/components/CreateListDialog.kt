package com.chemecador.secretaria.ui.view.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chemecador.secretaria.R

@Composable
fun CreateListDialog(
    showDialog: Boolean,
    initialName: String? = null,
    initialOrdered: Boolean? = null,
    onDismiss: () -> Unit,
    onCreate: (String, Boolean) -> Unit
) {
    if (!showDialog) return

    var listName by remember { mutableStateOf(initialName.orEmpty()) }
    var isOrdered by remember { mutableStateOf(initialOrdered ?: false) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.label_create_list)) },
        text = {
            Column {
                OutlinedTextField(
                    value = listName,
                    onValueChange = {
                        listName = it
                        if (it.isNotBlank()) isError = false
                    },
                    label = { Text(stringResource(R.string.label_list_name)) },
                    isError = isError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )
                if (isError) {
                    Text(
                        text = stringResource(R.string.error_empty_field),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Checkbox(
                        checked = isOrdered,
                        onCheckedChange = { isOrdered = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.Black,
                            uncheckedColor = Color.Black,
                            checkmarkColor = Color.White
                        )
                    )
                    Text(
                        text = stringResource(R.string.label_ordered_list),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (listName.isNotBlank()) {
                    onCreate(listName, isOrdered)
                    onDismiss()
                } else {
                    isError = true
                }
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
