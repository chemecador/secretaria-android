package com.chemecador.secretaria.ui.view.main


import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chemecador.secretaria.R
import com.chemecador.secretaria.core.Constants
import com.chemecador.secretaria.ui.theme.SecretariaTheme
import com.chemecador.secretaria.ui.view.friends.FriendsScreen
import com.chemecador.secretaria.ui.view.login.LoginActivity
import com.chemecador.secretaria.ui.view.main.screens.NoteDetailScreen
import com.chemecador.secretaria.ui.view.main.screens.NotesListsScreen
import com.chemecador.secretaria.ui.view.main.screens.NotesScreen
import com.chemecador.secretaria.ui.view.settings.AboutUsScreen
import com.chemecador.secretaria.ui.view.settings.SettingsScreen
import com.chemecador.secretaria.ui.viewmodel.main.MainViewModel
import com.chemecador.secretaria.ui.viewmodel.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            showNotificationSettingsDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (shouldRequestNotificationPermissionToday()) {
            requestNotificationPermission()
        }

        setContent {
            SecretariaTheme {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val route = backStackEntry?.destination?.route
                val appBarTitle = when (route) {

                    Constants.NOTES_LIST -> stringResource(R.string.title_noteslist)

                    "${Constants.NOTES}/{${Constants.LIST_ID}}/{${Constants.LIST_NAME}}/{${Constants.IS_ORDERED}}" ->
                        backStackEntry?.arguments?.getString(Constants.LIST_NAME)
                            ?: stringResource(R.string.title_noteslist)

                    "note_detail/{${Constants.LIST_ID}}/{${Constants.LIST_NAME}}/{${Constants.NOTE_ID}}" ->
                        backStackEntry?.arguments?.getString(Constants.LIST_NAME)
                            ?: stringResource(R.string.title_note_detail)

                    Constants.FRIENDS -> stringResource(R.string.label_friends)

                    Constants.ABOUT_US -> stringResource(R.string.title_activity_about_us)

                    Constants.SETTINGS -> stringResource(R.string.title_activity_settings)

                    Constants.SIGN_OUT -> stringResource(R.string.title_sign_out)

                    else -> stringResource(R.string.app_name)
                }

                Scaffold(
                    topBar = { SecretariaTopBar(navController, appBarTitle) }
                ) { padding ->
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(padding)
                    ) {
                        MainNavHost(navController)
                    }
                }
            }
        }
    }

    @Composable
    fun MainNavHost(navController: NavHostController) {
        NavHost(
            navController = navController,
            startDestination = Constants.NOTES_LIST,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Constants.NOTES_LIST) {
                NotesListsScreen(
                    onListClick = { listId, listName, isOrdered ->
                        navController.navigate("${Constants.NOTES}/$listId/$listName/$isOrdered")
                    }
                )
            }
            composable(
                route = "${Constants.NOTES}/{${Constants.LIST_ID}}/{${Constants.LIST_NAME}}/{${Constants.IS_ORDERED}}",
                arguments = listOf(
                    navArgument(Constants.LIST_ID) { type = NavType.StringType },
                    navArgument(Constants.LIST_NAME) { type = NavType.StringType },
                    navArgument(Constants.IS_ORDERED) { type = NavType.BoolType }
                )
            ) { backStackEntry ->
                val listId = backStackEntry.arguments?.getString(Constants.LIST_ID) ?: ""
                val listName = backStackEntry.arguments?.getString(Constants.LIST_NAME) ?: ""
                val isOrdered = backStackEntry.arguments?.getBoolean(Constants.IS_ORDERED) ?: false
                NotesScreen(
                    listId = listId,
                    isOrdered = isOrdered,
                    onNoteClick = { noteId ->
                        navController.navigate("note_detail/${listId}/${listName}/$noteId")
                    },
                )
            }
            composable(
                route = "note_detail/{${Constants.LIST_ID}}/{${Constants.LIST_NAME}}/{${Constants.NOTE_ID}}",
                arguments = listOf(
                    navArgument(Constants.LIST_ID) { type = NavType.StringType },
                    navArgument(Constants.LIST_NAME) { type = NavType.StringType },
                    navArgument(Constants.NOTE_ID) { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val listId = backStackEntry.arguments?.getString(Constants.LIST_ID) ?: ""
                val noteId = backStackEntry.arguments?.getString(Constants.NOTE_ID) ?: ""
                NoteDetailScreen(
                    listId = listId,
                    noteId = noteId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Constants.FRIENDS) {
                FriendsScreen()
            }
            composable(Constants.SETTINGS) {
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(viewModel = settingsViewModel)
            }
            composable(Constants.ABOUT_US) {
                val email = stringResource(R.string.contact_mail)
                val mailSubject = stringResource(R.string.label_mail_subject)
                AboutUsScreen(
                    onGithubClick = { url ->
                        startActivity(
                            Intent(Intent.ACTION_VIEW, url.toUri())
                        )
                    },
                    onContactClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:".toUri()
                            putExtra(Intent.EXTRA_EMAIL, email)
                            putExtra(Intent.EXTRA_SUBJECT, mailSubject)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SecretariaTopBar(navController: NavHostController, title: String) {
        val canPop = navController.previousBackStackEntry != null
        val expanded = remember { mutableStateOf(false) }
        val context = LocalContext.current
        TopAppBar(
            title = { Text(title) },
            navigationIcon = {
                if (canPop) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = { navController.navigate(Constants.FRIENDS) }) {
                    Icon(
                        imageVector = Icons.Filled.People,
                        contentDescription = stringResource(R.string.label_friends)
                    )
                }

                IconButton(onClick = { expanded.value = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                }
                DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.title_activity_settings)) },
                        onClick = {
                            expanded.value = false
                            navController.navigate(Constants.SETTINGS)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.title_activity_about_us)) },
                        onClick = {
                            expanded.value = false
                            navController.navigate(Constants.ABOUT_US)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.title_sign_out)) },
                        onClick = {
                            expanded.value = false

                            AlertDialog.Builder(context)
                                .setTitle(R.string.title_sign_out)
                                .setMessage(R.string.msg_sign_out)
                                .setPositiveButton(R.string.res_yes) { _, _ ->
                                    viewModel.signOut()
                                    startActivity(
                                        Intent(
                                            context,
                                            LoginActivity::class.java
                                        )
                                    )
                                    finish()
                                }
                                .setNegativeButton(R.string.action_cancel, null)
                                .setCancelable(true)
                                .show()
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }

    private fun shouldRequestNotificationPermissionToday(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        } else {
            if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                return false
            }
        }

        val sharedPrefs = getSharedPreferences("secretaria_prefs", MODE_PRIVATE)
        val lastRequestDate = sharedPrefs.getString("last_notification_request_date", "")
        val todayDate = getCurrentDateString()

        return lastRequestDate != todayDate
    }

    private fun getCurrentDateString(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
    }

    private fun markPermissionRequestedToday() {
        val sharedPrefs = getSharedPreferences("secretaria_prefs", MODE_PRIVATE)
        sharedPrefs.edit {
            putString("last_notification_request_date", getCurrentDateString())
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            val permissionStatus = ContextCompat.checkSelfPermission(this, permission)

            when {
                permissionStatus == PackageManager.PERMISSION_GRANTED -> {}
                shouldShowRequestPermissionRationale(permission) -> {
                    showNotificationSettingsDialog()
                }
                else -> {
                    requestNotificationPermissionLauncher.launch(permission)
                }
            }
        } else {
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                showNotificationSettingsDialog()
            }
        }
        markPermissionRequestedToday()
    }

    private fun showNotificationSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.title_notifications)
            .setMessage(R.string.msg_notifications)
            .setPositiveButton(R.string.res_go_to_settings) { _, _ ->
                val intent = Intent().apply {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }
                startActivity(intent)
            }
            .setNegativeButton(R.string.res_no) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
