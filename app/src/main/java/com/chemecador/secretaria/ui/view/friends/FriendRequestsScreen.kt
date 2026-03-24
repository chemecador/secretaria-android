package com.chemecador.secretaria.ui.view.friends

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.chemecador.secretaria.R
import com.chemecador.secretaria.data.model.Friendship
import com.chemecador.secretaria.ui.viewmodel.friends.FriendsViewModel
import com.chemecador.secretaria.utils.Resource


@Composable
fun FriendRequestsScreen(
    viewModel: FriendsViewModel = hiltViewModel()
) {
    val requestsRes by viewModel.friendRequests.collectAsState()
    val acceptRes by viewModel.acceptRequestStatus.collectAsState(null)
    val rejectRes by viewModel.rejectRequestStatus.collectAsState(null)

    LaunchedEffect(Unit) { viewModel.loadFriendRequests() }
    LaunchedEffect(acceptRes, rejectRes) {
        if (acceptRes is Resource.Success || rejectRes is Resource.Success) {
            viewModel.loadFriendRequests()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            requestsRes is Resource.Loading || acceptRes is Resource.Loading || rejectRes is Resource.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            requestsRes is Resource.Success -> {
                val list = (requestsRes as Resource.Success).data.orEmpty()
                if (list.isEmpty()) {
                    Text(
                        text = stringResource(R.string.label_no_requests),
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(items = list, key = { it.id }) { req ->
                            RequestActionItem(
                                request = req,
                                currentUserId = viewModel.getCurrentUserId() ?: "",
                                onAccept = { viewModel.acceptFriendRequest(req.id) },
                                onReject = { viewModel.rejectFriendRequest(req.id) }
                            )
                        }
                    }
                }
            }

            requestsRes is Resource.Error -> {
                val context = LocalContext.current
                LaunchedEffect(requestsRes) {
                    Toast.makeText(
                        context,
                        (requestsRes as Resource.Error).message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}

@Composable
fun RequestActionItem(
    request: Friendship,
    currentUserId: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val name = if (request.receiverId == currentUserId) request.senderName else request.receiverName
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = name, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onReject) {
                    Text(stringResource(R.string.action_reject))
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onAccept) {
                    Text(stringResource(R.string.action_accept))
                }
            }
        }
    }
}
