package com.chemecador.secretaria.ui.view.main.screens

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.chemecador.secretaria.R
import com.chemecador.secretaria.data.model.Note
import com.chemecador.secretaria.ui.view.components.CreateNoteDialog
import com.chemecador.secretaria.ui.viewmodel.main.NotesViewModel
import com.chemecador.secretaria.utils.Resource
import com.chemecador.secretaria.utils.SortOption
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    listId: String,
    viewModel: NotesViewModel = hiltViewModel(),
    onNoteClick: (noteId: String) -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf(SortOption.DATE_DESC) }
    var menuExpanded by remember { mutableStateOf(false) }


    var isRefreshing by remember { mutableStateOf(false) }
    val pullState = rememberPullToRefreshState()

    val notesState by viewModel.notes.collectAsState(initial = Resource.Loading())

    val sortOptions = stringArrayResource(R.array.sort_options)
    val sortLabel = when (sortOption) {
        SortOption.NAME_ASC -> sortOptions[0]
        SortOption.NAME_DESC -> sortOptions[1]
        SortOption.DATE_ASC -> sortOptions[2]
        SortOption.DATE_DESC -> sortOptions[3]
        SortOption.CUSTOM -> sortOptions[4]
    }

    LaunchedEffect(listId) {
        viewModel.fetchNotes(listId)
    }

    LaunchedEffect(notesState) {
        if (isRefreshing && notesState is Resource.Success) {
            isRefreshing = false
        }
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.label_create_note),
                    tint = MaterialTheme.colorScheme.background
                )
            }
        }
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = stringResource(R.string.label_order_by),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = sortLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(16.dp))
                Box(Modifier.wrapContentSize()) {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(R.string.action_sort)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.wrapContentSize(Alignment.TopEnd)
                    ) {
                        sortOptions.forEachIndexed { index, title ->
                            val option = when (index) {
                                0 -> SortOption.NAME_ASC
                                1 -> SortOption.NAME_DESC
                                2 -> SortOption.DATE_ASC
                                3 -> SortOption.DATE_DESC
                                else -> SortOption.CUSTOM
                            }
                            DropdownMenuItem(
                                text = { Text(title) },
                                onClick = {
                                    sortOption = option
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    viewModel.fetchNotes(listId)
                },
                state = pullState,
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = notesState) {
                    is Resource.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    is Resource.Error -> Text(
                        text = state.message.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center
                    )

                    is Resource.Success -> {
                        val notes = remember(state.data, sortOption) {
                            state.data.orEmpty()
                                .let { unsorted ->
                                    when (sortOption) {
                                        SortOption.NAME_ASC -> unsorted.sortedBy { it.title.lowercase() }
                                        SortOption.NAME_DESC -> unsorted.sortedByDescending { it.title.lowercase() }
                                        SortOption.DATE_ASC -> unsorted.sortedBy { it.date }
                                        SortOption.DATE_DESC -> unsorted.sortedByDescending { it.date }
                                        SortOption.CUSTOM -> unsorted.sortedBy { it.order }
                                    }
                                }
                        }

                        if (notes.isEmpty()) {
                            Text(
                                text = stringResource(R.string.label_empty_notes),
                                modifier = Modifier.align(Alignment.Center),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            DraggableNotesList(
                                notes = notes,
                                showOrder = sortOption == SortOption.CUSTOM,
                                onNoteClick = onNoteClick,
                                onReorder = { reordered ->
                                    sortOption = SortOption.CUSTOM
                                    viewModel.reorderNotes(listId, reordered)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    CreateNoteDialog(
        showDialog = showDialog,
        onDismiss = { showDialog = false },
        onCreate = { title, content ->
            viewModel.createNote(
                listId,
                Note(
                    title = title,
                    content = content,
                    date = Timestamp.now(),
                    creator = viewModel.getUsername(),
                    color = Color.White.toArgb()
                )
            )
        }
    )
}

@Composable
private fun DraggableNotesList(
    notes: List<Note>,
    showOrder: Boolean,
    onNoteClick: (noteId: String) -> Unit,
    onReorder: (List<Note>) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val dragList = remember(notes) { notes.toMutableStateList() }
    var draggedIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        lazyListState.layoutInfo.visibleItemsInfo
                            .firstOrNull { item ->
                                offset.y.toInt() in item.offset..(item.offset + item.size)
                            }
                            ?.let { item ->
                                draggedIndex = item.index
                                dragOffset = 0f
                            }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount.y

                        val dragged = lazyListState.layoutInfo.visibleItemsInfo
                            .firstOrNull { it.index == draggedIndex }
                            ?: return@detectDragGesturesAfterLongPress

                        val draggedCenter = dragged.offset + dragged.size / 2 + dragOffset.toInt()

                        val target = lazyListState.layoutInfo.visibleItemsInfo
                            .firstOrNull { item ->
                                item.index != draggedIndex &&
                                        draggedCenter in item.offset..(item.offset + item.size)
                            }

                        if (target != null) {
                            val targetIndex = target.index
                            dragList.apply {
                                val item = removeAt(draggedIndex)
                                add(targetIndex, item)
                            }
                            val offsetAdjust = if (targetIndex > draggedIndex) {
                                -target.size.toFloat()
                            } else {
                                target.size.toFloat()
                            }
                            dragOffset += offsetAdjust
                            draggedIndex = targetIndex
                        }
                    },
                    onDragEnd = {
                        if (draggedIndex >= 0) {
                            onReorder(dragList.toList())
                        }
                        draggedIndex = -1
                        dragOffset = 0f
                    },
                    onDragCancel = {
                        draggedIndex = -1
                        dragOffset = 0f
                    }
                )
            },
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(dragList, key = { _, note -> note.id }) { index, note ->
            val isDragging = index == draggedIndex
            val elevation by animateDpAsState(
                targetValue = if (isDragging) 8.dp else 0.dp,
                label = "dragElevation"
            )

            NoteItem(
                note = note,
                orderNumber = if (showOrder) index + 1 else null,
                onClick = { if (!isDragging) onNoteClick(note.id) },
                modifier = Modifier
                    .then(
                        if (isDragging) {
                            Modifier
                                .zIndex(1f)
                                .graphicsLayer { translationY = dragOffset }
                                .shadow(elevation, RoundedCornerShape(8.dp))
                        } else {
                            Modifier.animateItem()
                        }
                    )
            )
        }
    }
}

@Composable
fun NoteItem(
    note: Note,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    orderNumber: Int? = null
) {
    val dateString = remember(note.date) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.format(note.date.toDate())
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (orderNumber != null) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = orderNumber.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.surface
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            textDecoration = if (note.completed) TextDecoration.LineThrough else TextDecoration.None
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textDecoration = if (note.completed) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dateString,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = note.creator,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        if (note.completed) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.label_completed),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(36.dp)
                    .padding(8.dp)
                    .background(
                        color = Color(note.color),
                        shape = CircleShape
                    )
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = CircleShape
                    )
            )
        }
    }
}
