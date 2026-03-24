package com.chemecador.secretaria.ui.view.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.chemecador.secretaria.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CascadingDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier.wrapContentSize(Alignment.TopStart)
    ) {
        val items = listOf(
            Triple(stringResource(R.string.action_share), Icons.Default.Share, onShare),
            Triple(stringResource(R.string.action_edit), Icons.Default.Edit, onEdit),
            Triple(stringResource(R.string.action_delete), Icons.Default.Delete, onDelete)
        )
        var itemVisible by remember { mutableStateOf(List(items.size) { false }) }

        LaunchedEffect(expanded) {
            if (expanded) {
                items.indices.forEach { idx ->
                    delay(idx * 75L)
                    itemVisible = itemVisible.toMutableList().also { it[idx] = true }
                }
            } else {
                itemVisible = List(items.size) { false }
            }
        }
        items.forEachIndexed { index, (label, icon, action) ->
            AnimatedVisibility(
                visible = itemVisible[index],
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(durationMillis = 150))
            ) {
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        action()
                        onDismissRequest()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (label == stringResource(R.string.action_delete)) MaterialTheme.colorScheme.error else LocalContentColor.current
                        )
                    }
                )
            }
        }
    }
}