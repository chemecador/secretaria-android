package com.chemecador.secretaria.ui.view.friends

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.chemecador.secretaria.R
import com.chemecador.secretaria.data.model.Friendship
import com.chemecador.secretaria.ui.view.components.ConfirmationDialog
import com.chemecador.secretaria.ui.viewmodel.friends.FriendsViewModel
import com.chemecador.secretaria.utils.Resource

@Composable
fun FriendListScreen(
    viewModel: FriendsViewModel = hiltViewModel()
) {
    val friendshipsRes by viewModel.friendships.collectAsState()
    val deleteStatus by viewModel.deleteFriendStatus.collectAsState(null)
    var toDelete by remember { mutableStateOf<Friendship?>(null) }

    LaunchedEffect(Unit) { viewModel.loadFriendships() }
    LaunchedEffect(deleteStatus) {
        if (deleteStatus is Resource.Success) {
            viewModel.loadFriendships()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            friendshipsRes is Resource.Loading || deleteStatus is Resource.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            friendshipsRes is Resource.Success -> {
                val list = (friendshipsRes as Resource.Success).data.orEmpty()
                if (list.isEmpty()) {
                    Text(
                        text = stringResource(R.string.label_no_friends),
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = list,
                            key = { it.id }
                        ) { friend ->
                            FriendItem(
                                friend = friend,
                                currentUserId = viewModel.getCurrentUserId() ?: "",
                                onRequestDelete = { toDelete = friend }
                            )
                        }
                    }
                }
            }

            friendshipsRes is Resource.Error -> {
                val context = LocalContext.current
                LaunchedEffect(friendshipsRes) {
                    Toast
                        .makeText(
                            context,
                            (friendshipsRes as Resource.Error).message,
                            Toast.LENGTH_LONG
                        )
                        .show()
                }
            }
        }
        toDelete?.let { friend ->
            ConfirmationDialog(
                title = stringResource(R.string.dialog_delete_friend_title),
                text = stringResource(
                    R.string.dialog_delete_friend_msg,
                    if (friend.receiverId == viewModel.getCurrentUserId()) friend.senderName else friend.receiverName
                ),
                onConfirm = {
                    viewModel.deleteFriend(friend.id)
                    toDelete = null
                },
                onDismiss = { toDelete = null }
            )
        }
    }
}

@Composable
fun FriendItem(
    friend: Friendship,
    currentUserId: String,
    onRequestDelete: () -> Unit
) {
    val name = if (friend.receiverId == currentUserId) friend.senderName else friend.receiverName
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRequestDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.action_delete)
                )
            }
        }
    }
}
