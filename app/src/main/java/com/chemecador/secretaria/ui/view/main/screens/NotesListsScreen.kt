package com.chemecador.secretaria.ui.view.main.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.chemecador.secretaria.R
import com.chemecador.secretaria.data.model.NotesList
import com.chemecador.secretaria.ui.view.components.CascadingDropdownMenu
import com.chemecador.secretaria.ui.view.components.ConfirmationDialog
import com.chemecador.secretaria.ui.view.components.CreateListDialog
import com.chemecador.secretaria.ui.view.components.ShareListDialog
import com.chemecador.secretaria.ui.viewmodel.main.NotesListViewModel
import com.chemecador.secretaria.utils.Resource
import com.chemecador.secretaria.utils.SortOption
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListsScreen(
    viewModel: NotesListViewModel = hiltViewModel(),
    onListClick: (listId: String, listName: String, isOrdered: Boolean) -> Unit = { _, _, _ -> }
) {
    var showDialog by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf(SortOption.DATE_DESC) }
    var menuExpanded by remember { mutableStateOf(false) }

    var isRefreshing by remember { mutableStateOf(false) }
    val pullState = rememberPullToRefreshState()

    val notesLists by viewModel.notesLists.collectAsState()
    var shareDialogListId by remember { mutableStateOf<String?>(null) }
    var deleteDialogListId by remember { mutableStateOf<String?>(null) }
    var editDialogData by remember { mutableStateOf<NotesList?>(null) }

    val sortOptions = stringArrayResource(R.array.sort_options)
    val sortLabel = when (sortOption) {
        SortOption.NAME_ASC -> sortOptions[0]
        SortOption.NAME_DESC -> sortOptions[1]
        SortOption.DATE_ASC -> sortOptions[2]
        SortOption.DATE_DESC -> sortOptions[3]
        SortOption.CUSTOM -> sortOptions[4]
    }

    LaunchedEffect(viewModel.updateStatus) {
        viewModel.updateStatus.collect { status ->
            if (status is Resource.Success) {
                viewModel.fetchLists()
            }
        }
    }

    LaunchedEffect(viewModel.deleteStatus) {
        viewModel.deleteStatus.collect { status ->
            if (status is Resource.Success) {
                viewModel.fetchLists()
            }
        }
    }

    LaunchedEffect(notesLists) {
        if (isRefreshing && notesLists is Resource.Success) {
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
                    contentDescription = stringResource(R.string.label_create_list),
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
                    .padding(
                        horizontal = dimensionResource(R.dimen.margin_medium),
                        vertical = dimensionResource(R.dimen.margin_xxsmall)
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = stringResource(R.string.label_order_by),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.margin_small)))
                Text(
                    text = sortLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.margin_medium)))
                Box(modifier = Modifier.wrapContentSize()) {

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
                        sortOptions.forEachIndexed { idx, title ->
                            val option = when (idx) {
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
                    viewModel.fetchLists()
                },
                state = pullState,
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = notesLists) {
                    is Resource.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    is Resource.Success -> {
                        val lists = state.data.orEmpty()
                            .let { unsorted ->
                                when (sortOption) {
                                    SortOption.NAME_ASC -> unsorted.sortedBy { it.name.lowercase() }
                                    SortOption.NAME_DESC -> unsorted.sortedByDescending { it.name.lowercase() }
                                    SortOption.DATE_ASC -> unsorted.sortedBy { it.date }
                                    SortOption.DATE_DESC -> unsorted.sortedByDescending { it.date }
                                    SortOption.CUSTOM -> unsorted.sortedByDescending { it.date }
                                }
                            }
                        if (lists.isEmpty()) {
                            Text(
                                text = stringResource(R.string.label_empty_lists),
                                modifier = Modifier.align(Alignment.Center),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(lists) { list ->
                                    val dateString = remember(list.date) {
                                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                            .format(list.date.toDate())
                                    }
                                    NotesListItem(
                                        title = list.name,
                                        creator = list.creator,
                                        date = dateString,
                                        onItemClick = { onListClick(list.id, list.name, list.ordered) },
                                        onShare = { shareDialogListId = list.id },
                                        onEdit = { editDialogData = list },
                                        onDelete = { deleteDialogListId = list.id }
                                    )
                                }
                            }
                        }
                    }

                    is Resource.Error -> {
                        Text(
                            text = "Error: ${state.message}",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            CreateListDialog(
                showDialog = showDialog,
                onDismiss = { showDialog = false },
                onCreate = { name, ordered -> viewModel.createList(name, ordered) }
            )
        }
    }

    shareDialogListId?.let { listId ->
        ShareListDialog(
            listId = listId,
            onDismissRequest = { shareDialogListId = null }
        )
    }

    deleteDialogListId?.let { listId ->
        ConfirmationDialog(
            title = stringResource(R.string.dialog_delete_list_title),
            text = stringResource(R.string.dialog_delete_list_msg),
            onConfirm = {
                viewModel.deleteList(listId)
                deleteDialogListId = null
            },
            onDismiss = { deleteDialogListId = null }
        )
    }

    editDialogData?.let { currentList ->
        CreateListDialog(
            showDialog = true,
            initialName = currentList.name,
            initialOrdered = currentList.ordered,
            onDismiss = { editDialogData = null },
            onCreate = { newName, ordered ->
                viewModel.editList(currentList.copy(name = newName, ordered = ordered))
                editDialogData = null
            }
        )
    }
}

@Composable
fun NotesListItem(
    title: String,
    creator: String,
    date: String,
    onItemClick: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_small)),
        elevation = CardDefaults.cardElevation(dimensionResource(R.dimen.cardview_default_elevation)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
    ) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.margin_medium))) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_more_vert),
                            contentDescription = stringResource(R.string.label_more)
                        )
                    }
                    CascadingDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        onShare = onShare,
                        onEdit = onEdit,
                        onDelete = onDelete
                    )
                }
            }
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.margin_small)))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = creator,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
