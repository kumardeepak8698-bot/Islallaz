package com.example

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    // Modern registerForActivityResult to handle Enterprise managed provisioning callback
    private val provisionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "Profile provisioning initiated successfully", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Profile provisioning completed or dismissed", Toast.LENGTH_SHORT).show()
        }
        viewModel.checkStatus()
        viewModel.refreshApps()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                
                val isRunningInIsland by viewModel.isRunningInIsland.collectAsStateWithLifecycle()
                val isIslandConfigured by viewModel.isIslandConfigured.collectAsStateWithLifecycle()
                val isProfileOwner by viewModel.isProfileOwner.collectAsStateWithLifecycle()
                
                val mainlandApps by viewModel.mainlandApps.collectAsStateWithLifecycle()
                val islandApps by viewModel.islandApps.collectAsStateWithLifecycle()
                val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
                val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
                val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
                val appConfigs by viewModel.appConfigs.collectAsStateWithLifecycle()

                val context = LocalContext.current

                // Show basic floating info snackbar for VM connection errors
                LaunchedEffect(errorMessage) {
                    errorMessage?.let {
                        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                        viewModel.clearError()
                    }
                }

                // Determine start destination on launch
                val startDestination = if (isIslandConfigured || isRunningInIsland) {
                    "dashboard"
                } else {
                    "welcome"
                }

                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Welcome onboard screen
                    composable("welcome") {
                        WelcomeOnboardingScreen(
                            onNavigateToSetup = { navController.navigate("setup") },
                            onNavigateToDashboard = { navController.navigate("dashboard") },
                            isAlreadyConfigured = isIslandConfigured || isRunningInIsland
                        )
                    }

                    // Setup manager options screen
                    composable("setup") {
                        ProfileSetupScreen(
                            isAlreadyConfigured = isIslandConfigured || isRunningInIsland,
                            onProvisionClick = {
                                val intent = viewModel.getProvisioningIntent()
                                provisionLauncher.launch(intent)
                            },
                            onNavigateToDashboard = { navController.navigate("dashboard") },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // Consolidated dashboard screen with clean Material 3 Bottom Navigation
                    composable("dashboard") {
                        var activeTabIdx by remember { mutableStateOf(0) }

                        Scaffold(
                            bottomBar = {
                                NavigationBar(
                                    modifier = Modifier.testTag("dashboard_bottom_bar")
                                ) {
                                    NavigationBarItem(
                                        selected = activeTabIdx == 0,
                                        onClick = { activeTabIdx = 0 },
                                        icon = { Icon(Icons.Default.Public, contentDescription = "Mainland Tap") },
                                        label = { Text("Mainland") },
                                        modifier = Modifier.testTag("tab_mainland")
                                    )
                                    NavigationBarItem(
                                        selected = activeTabIdx == 1,
                                        onClick = { activeTabIdx = 1 },
                                        icon = { Icon(Icons.Default.Lock, contentDescription = "Island Tap") },
                                        label = { Text("Island") },
                                        modifier = Modifier.testTag("tab_island")
                                    )
                                    NavigationBarItem(
                                        selected = activeTabIdx == 2,
                                        onClick = { activeTabIdx = 2 },
                                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings Tap") },
                                        label = { Text("Settings") },
                                        modifier = Modifier.testTag("tab_settings")
                                    )
                                    NavigationBarItem(
                                        selected = activeTabIdx == 3,
                                        onClick = { activeTabIdx = 3 },
                                        icon = { Icon(Icons.Default.Info, contentDescription = "About Tap") },
                                        label = { Text("About") },
                                        modifier = Modifier.testTag("tab_about")
                                    )
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                when (activeTabIdx) {
                                    0 -> AppListScreen(
                                        apps = mainlandApps,
                                        configs = appConfigs,
                                        searchQuery = searchQuery,
                                        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                        isLoading = isLoading,
                                        onRefresh = { viewModel.refreshApps() },
                                        onCloneClick = { pkg, onSuccess, onError ->
                                            viewModel.cloneApp(pkg, onSuccess, onError)
                                        },
                                        onSaveConfig = { pkg, alias, cat, notes, autoFrz ->
                                            viewModel.saveAppConfig(pkg, alias, cat, notes, autoFrz)
                                        }
                                    )
                                    1 -> ClonedAppsScreen(
                                        apps = islandApps,
                                        configs = appConfigs,
                                        searchQuery = searchQuery,
                                        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                        isLoading = isLoading,
                                        isProfileOwner = isProfileOwner,
                                        isIslandProfileConfigured = isIslandConfigured || isRunningInIsland,
                                        onRefresh = { viewModel.refreshApps() },
                                        onFreezeClick = { pkg, onSuccess, onError ->
                                            viewModel.freezeApp(pkg, onSuccess, onError)
                                        },
                                        onUnfreezeClick = { pkg, onSuccess, onError ->
                                            viewModel.unfreezeApp(pkg, onSuccess, onError)
                                        },
                                        onLaunchClick = { pkg ->
                                            launchIslandApp(pkg)
                                        },
                                        onUninstallClick = { pkg ->
                                            uninstallIslandApp(pkg)
                                        },
                                        onNavigateToSetup = { navController.navigate("setup") },
                                        onSaveConfig = { pkg, alias, cat, notes, autoFrz ->
                                            viewModel.saveAppConfig(pkg, alias, cat, notes, autoFrz)
                                        }
                                    )
                                    2 -> SettingsScreen(
                                        isIslandConfigured = isIslandConfigured,
                                        isRunningInIsland = isRunningInIsland,
                                        isProfileOwner = isProfileOwner,
                                        onWipeClick = { onSuccess, onError ->
                                            viewModel.wipeProfile(onSuccess, onError)
                                        },
                                        onNavigateToSetup = { navController.navigate("setup") },
                                        onNavigateToOnboarding = { navController.navigate("welcome") }
                                    )
                                    3 -> AboutScreen()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Launch a cloned/isolated app in the Work Profile using LauncherApps
    private fun launchIslandApp(packageName: String) {
        try {
            val launcherApps = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            val userManager = getSystemService(Context.USER_SERVICE) as android.os.UserManager
            val currentProfile = android.os.Process.myUserHandle()
            val islandUser = userManager.userProfiles.firstOrNull { it != currentProfile }

            if (islandUser != null) {
                val activities = launcherApps.getActivityList(packageName, islandUser)
                if (activities.isNotEmpty()) {
                    launcherApps.startMainActivity(activities[0].componentName, islandUser, null, null)
                    Toast.makeText(this, "Launching ${activities[0].label} in Sandbox", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "App is frozen or has no launchable launcher activity in Sandbox.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Cannot locate sandbox user profile handle on this system.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to launch app: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // Direct uninstallation of selected cloned package inside Work profile
    private fun uninstallIslandApp(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to initiate package deletion: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkStatus()
        viewModel.refreshApps()
    }
}
