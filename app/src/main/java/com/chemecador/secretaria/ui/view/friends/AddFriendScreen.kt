package com.chemecador.secretaria.ui.view.friends

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.chemecador.secretaria.R
import com.chemecador.secretaria.data.model.Friendship
import com.chemecador.secretaria.ui.view.components.ConfirmationDialog
import com.chemecador.secretaria.ui.view.components.SecretariaButton
import com.chemecador.secretaria.ui.viewmodel.friends.FriendsViewModel
import com.chemecador.secretaria.utils.Resource

@Composable
fun AddFriendScreen(
    viewModel: FriendsViewModel = hiltViewModel()
) {
    val userCode by viewModel.userCode.collectAsState("")
    val addStatus by viewModel.addFriendStatus.collectAsState(null)
    val sentRequestsRes by viewModel.friendRequestsSent.collectAsState()
    var friendCode by remember { mutableStateOf("") }
    var friendCodeError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadUserCode()
        viewModel.loadFriendRequestsSent()
    }
    LaunchedEffect(addStatus) {
        if (addStatus is Resource.Success) {
            viewModel.loadFriendRequestsSent()
            friendCode = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.label_add_friend),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.label_your_code))
        Text(
            text = userCode.orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = friendCode,
            onValueChange = {
                friendCode = it
                friendCodeError = null
            },
            label = { Text(stringResource(R.string.label_friend_code)) },
            isError = friendCodeError != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        friendCodeError?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }

        Spacer(Modifier.height(8.dp))

        val invalidCodeMsg = stringResource(R.string.error_invalid_friendcode)
        val alreadySentMsg = stringResource(R.string.error_friend_request_already_sent)

        SecretariaButton(
            onClick = {
                val valid = try {
                    if (friendCode.length < 3 || friendCode == userCode) throw IllegalArgumentException()
                    val list = (sentRequestsRes as? Resource.Success)?.data.orEmpty()
                    if (list.any { it.receiverCode == friendCode }) throw IllegalStateException()
                    true
                } catch (e: Exception) {
                    friendCodeError =
                        if (e is IllegalStateException) alreadySentMsg else invalidCodeMsg
                    false
                }
                if (valid) viewModel.sendFriendRequest(friendCode)
            },
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.label_send_request),
            icon = Icons.Default.PersonAdd
        )

        Spacer(Modifier.height(dimensionResource(R.dimen.margin_xxlarge)))
        Text(
            text = stringResource(R.string.label_friend_requests_sent),
            style = MaterialTheme.typography.titleLarge
        )

        when {
            addStatus is Resource.Loading || sentRequestsRes is Resource.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            addStatus is Resource.Success -> {
                val msg = stringResource(R.string.label_friend_request_sent)
                LaunchedEffect(addStatus) {
                    SnackbarHostState().showSnackbar(msg)
                }
            }

            addStatus is Resource.Error -> {
                val context = LocalContext.current
                LaunchedEffect(addStatus) {
                    Toast.makeText(
                        context,
                        (addStatus as Resource.Error).message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        val list = (sentRequestsRes as? Resource.Success)?.data.orEmpty()
        if (list.isEmpty() && sentRequestsRes is Resource.Success) {
            Text(
                text = stringResource(R.string.label_empty_requests_sent),
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxHeight()) {
                items(items = list, key = { it.id }) { request ->
                    RequestItem(
                        request = request,
                        onCancel = { viewModel.cancelFriendRequest(request.id) })
                }
            }
        }
    }
}

@Composable
fun RequestItem(
    request: Friendship,
    onCancel: () -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }
    val dateFormatter = remember {
        java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
    }
    val date = remember(request.requestDate) {
        dateFormatter.format(request.requestDate.toDate())
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(request.receiverCode, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.margin_medium)))
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = { showConfirm = true }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.action_cancel)
                )
            }
        }
    }
    if (showConfirm) {
        ConfirmationDialog(
            title = stringResource(R.string.dialog_cancel_request_title),
            text = stringResource(R.string.dialog_cancel_request_msg),
            confirmText = stringResource(R.string.dialog_cancel_cancel),
            dismissText = stringResource(R.string.dialog_cancel_accept),
            onConfirm = {
                onCancel()
                showConfirm = false
            },
            onDismiss = {
                showConfirm = false
            }
        )
    }
}

