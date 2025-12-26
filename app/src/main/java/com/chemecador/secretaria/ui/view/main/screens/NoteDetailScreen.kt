package com.chemecador.secretaria.ui.view.main.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chemecador.secretaria.R
import com.chemecador.secretaria.data.model.Note
import com.chemecador.secretaria.ui.view.components.ConfirmationDialog
import com.chemecador.secretaria.ui.view.components.SecretariaCheckbox
import com.chemecador.secretaria.ui.viewmodel.main.NoteDetailViewModel
import com.chemecador.secretaria.utils.Resource
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    listId: String,
    noteId: String,
    viewModel: NoteDetailViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    LaunchedEffect(noteId, listId) {
        viewModel.getNote(listId, noteId)
    }

    val noteResource by viewModel.note.observeAsState(initial = Resource.Loading())

    var editMode by remember { mutableStateOf(false) }
    var titleText by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }
    var checkboxState by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableIntStateOf(Color.White.toArgb()) }

    val scrollState = rememberScrollState()

    LaunchedEffect(noteResource) {
        if (noteResource is Resource.Success) {
            (noteResource as Resource.Success<Note>).data?.let { note ->
                if (!editMode) {
                    titleText = note.title
                    contentText = note.content
                    checkboxState = note.completed
                    selectedColor = note.color
                }
            }
        }
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (noteResource) {
                is Resource.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is Resource.Error -> {
                    Text(
                        text = stringResource(
                            R.string.error_generic,
                            (noteResource as Resource.Error).message.orEmpty()
                        ),
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                is Resource.Success -> {
                    val note = (noteResource as Resource.Success<Note>).data
                    if (note == null) {
                        Text(
                            text = stringResource(R.string.error_note_not_found),
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(dimensionResource(R.dimen.margin_medium))
                                .verticalScroll(scrollState)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(
                                                color = Color(selectedColor),
                                                shape = CircleShape
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = Color.Gray,
                                                shape = CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.margin_small)))
                                    val dateString = remember(note.date) {
                                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                            .format(note.date.toDate())
                                    }
                                    Text(
                                        text = dateString,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Text(
                                    text = note.creator,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.margin_large)))

                            if (!editMode) {
                                Text(
                                    text = titleText,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        textDecoration = if (checkboxState) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                )
                            } else {
                                OutlinedTextField(
                                    value = titleText,
                                    onValueChange = { titleText = it },
                                    label = { Text(stringResource(R.string.label_note_title)) },
                                    isError = titleText.isBlank(),
                                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (titleText.isBlank()) {
                                    Text(
                                        text = stringResource(R.string.error_empty_field),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(
                                            top = dimensionResource(R.dimen.margin_xsmall)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.margin_large)))

                            if (!editMode) {
                                Text(
                                    text = contentText,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        textDecoration = if (checkboxState) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                )
                            } else {
                                OutlinedTextField(
                                    value = contentText,
                                    onValueChange = { contentText = it },
                                    label = { Text(stringResource(R.string.label_note_content)) },
                                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = dimensionResource(R.dimen.margin_xxlarge)),
                                    maxLines = 5
                                )
                            }

                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.margin_large)))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SecretariaCheckbox(
                                    checked = checkboxState,
                                    onCheckedChange = {
                                        checkboxState = it
                                        editMode = true
                                    }
                                )
                                Text(
                                    text = if (checkboxState) stringResource(R.string.label_completed) else stringResource(
                                        R.string.label_not_completed
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = dimensionResource(R.dimen.margin_small))
                                )
                            }

                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.margin_large)))

                            ColorSelector(
                                selectedColor = selectedColor,
                                onColorSelected = {
                                    if (selectedColor != it) {
                                        selectedColor = it
                                        if (!editMode) {
                                            editMode = true
                                        }
                                    } else {
                                        selectedColor = it
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.margin_large)))

                            if (!editMode) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    OutlinedButton(
                                        onClick = { editMode = true },
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = stringResource(R.string.action_edit)
                                        )
                                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.margin_xsmall)))
                                        Text(stringResource(R.string.action_edit))
                                    }
                                    OutlinedButton(
                                        onClick = { showDeleteDialog = true },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.action_delete)
                                        )
                                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.margin_xsmall)))
                                        Text(stringResource(R.string.action_delete))
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            editMode = false
                                            noteResource.data?.let {
                                                titleText = it.title
                                                contentText = it.content
                                                checkboxState = it.completed
                                                selectedColor = it.color
                                            }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = stringResource(R.string.action_cancel)
                                        )
                                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.margin_xsmall)))
                                        Text(stringResource(R.string.action_cancel))
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            if (titleText.isBlank()) return@OutlinedButton
                                            val updatedNote = note.copy(
                                                title = titleText,
                                                content = contentText,
                                                date = Timestamp.now(),
                                                creator = viewModel.getUsername(),
                                                completed = checkboxState,
                                                color = selectedColor
                                            )
                                            viewModel.editNote(listId, updatedNote)
                                            editMode = false
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = stringResource(R.string.action_confirm)
                                        )
                                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.margin_xsmall)))
                                        Text(stringResource(R.string.action_confirm))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.margin_large)))
                        }
                    }
                }
            }
        }

        if (showDeleteDialog) {
            ConfirmationDialog(
                title = stringResource(R.string.dialog_delete_note_title),
                text = stringResource(R.string.dialog_delete_note_msg),
                onConfirm = {
                    viewModel.deleteNote(listId, noteId)
                    showDeleteDialog = false
                    onBack()
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
    }
}

@Composable
fun ColorSelector(
    selectedColor: Int,
    onColorSelected: (Int) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val colors = listOf(
        Color.White.toArgb(),
        Color(0xFFE0E0E0).toArgb(),
        Color(0xFFBDBDBD).toArgb(),
        Color(0xFF9E9E9E).toArgb(),

        Color(0xFFFFCDD2).toArgb(),
        Color(0xFFF8BBD0).toArgb(),
        Color(0xFFE91E63).toArgb(),
        Color(0xFFFF5722).toArgb(),
        Color(0xFFF44336).toArgb(),

        Color(0xFFE1BEE7).toArgb(),
        Color(0xFFD1C4E9).toArgb(),
        Color(0xFF9C27B0).toArgb(),
        Color(0xFF673AB7).toArgb(),
        Color(0xFF3F51B5).toArgb(),

        Color(0xFFBBDEFB).toArgb(),
        Color(0xFF90CAF9).toArgb(),
        Color(0xFF2196F3).toArgb(),
        Color(0xFF1976D2).toArgb(),
        Color(0xFF0D47A1).toArgb(),

        Color(0xFFB2DFDB).toArgb(),
        Color(0xFF80CBC4).toArgb(),
        Color(0xFF009688).toArgb(),
        Color(0xFF00BCD4).toArgb(),
        Color(0xFF0097A7).toArgb(),

        Color(0xFFC8E6C9).toArgb(),
        Color(0xFFA5D6A7).toArgb(),
        Color(0xFF4CAF50).toArgb(),
        Color(0xFF8BC34A).toArgb(),
        Color(0xFF689F38).toArgb(),

        Color(0xFFF0F4C3).toArgb(),
        Color(0xFFFFF9C4).toArgb(),
        Color(0xFFFFEB3B).toArgb(),
        Color(0xFFCDDC39).toArgb(),
        Color(0xFFAFB42B).toArgb(),

        Color(0xFFFFE0B2).toArgb(),
        Color(0xFFFFB74D).toArgb(),
        Color(0xFFFF9800).toArgb(),
        Color(0xFFFFC107).toArgb(),
        Color(0xFFFF8F00).toArgb(),

        Color(0xFFD7CCC8).toArgb(),
        Color(0xFFBCAAA4).toArgb(),
        Color(0xFF795548).toArgb(),
        Color(0xFF607D8B).toArgb(),
        Color(0xFF455A64).toArgb()
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.label_note_color),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.margin_small)))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = Color(selectedColor),
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = Color.Gray,
                            shape = CircleShape
                        )
                )
            }

            IconButton(
                onClick = { isExpanded = !isExpanded }
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded)
                        stringResource(R.string.action_collapse_colors)
                    else
                        stringResource(R.string.action_expand_colors),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.margin_small)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.margin_small)),
                modifier = Modifier
                    .heightIn(max = 300.dp)
                    .padding(top = dimensionResource(R.dimen.margin_small))
            ) {
                items(colors) { color ->
                    ColorOption(
                        color = color,
                        isSelected = color == selectedColor,
                        onClick = {
                            onColorSelected(color)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ColorOption(
    color: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable { onClick() }
            .background(
                color = Color(color),
                shape = CircleShape
            )
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                shape = CircleShape
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Color seleccionado",
                tint = if (color == Color.White.toArgb()) Color.Black else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}