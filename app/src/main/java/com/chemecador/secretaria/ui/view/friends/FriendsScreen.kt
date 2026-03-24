package com.chemecador.secretaria.ui.view.friends

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.chemecador.secretaria.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentRoute =
                    navController.currentBackStackEntryAsState().value?.destination?.route
                FriendRoute.all.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(stringResource(screen.label)) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController,
            startDestination = FriendRoute.List.route,
            Modifier.padding(padding)
        ) {
            composable(FriendRoute.List.route) {
                FriendListScreen(
                    viewModel = hiltViewModel(),
                )
            }
            composable(FriendRoute.Requests.route) {
                FriendRequestsScreen(viewModel = hiltViewModel())
            }
            composable(FriendRoute.Add.route) {
                AddFriendScreen(viewModel = hiltViewModel())
            }
        }
    }
}

sealed class FriendRoute(val route: String, @StringRes val label: Int, val icon: ImageVector) {
    object List : FriendRoute("friend_list", R.string.label_friends, Icons.Filled.People)
    object Requests :
        FriendRoute("friend_requests", R.string.label_friend_requests, Icons.Filled.Notifications)

    object Add : FriendRoute("add_friend", R.string.label_add_friend, Icons.Filled.PersonAddAlt1)
    companion object {
        val all = listOf(List, Requests, Add)
    }
}
