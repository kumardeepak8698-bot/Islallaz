package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppConfigEntity
import com.example.model.AppInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClonedAppsScreen(
    apps: List<AppInfo>,
    configs: Map<String, AppConfigEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isLoading: Boolean,
    isProfileOwner: Boolean,
    isIslandProfileConfigured: Boolean,
    onRefresh: () -> Unit,
    onFreezeClick: (String, () -> Unit, (String) -> Unit) -> Unit,
    onUnfreezeClick: (String, () -> Unit, (String) -> Unit) -> Unit,
    onLaunchClick: (String) -> Unit,
    onUninstallClick: (String) -> Unit,
    onNavigateToSetup: () -> Unit,
    onSaveConfig: (String, String?, String, String, Boolean) -> Unit
) {
    var showEditDialog by remember { mutableStateOf<AppInfo?>(null) }
    var editLabel by remember { mutableStateOf("") }
    var editNotes by remember { mutableStateOf("") }
    var editCategory by remember { mutableStateOf("Work") }
    var editAutoFreeze by remember { mutableStateOf(false) }

    // Filter apps based on search
    val filteredApps = apps.filter {
        it.appName.contains(searchQuery, ignoreCase = true) || 
        it.packageName.contains(searchQuery, ignoreCase = true)
    }

    // Split apps into frozen and active
    val activeApps = filteredApps.filter { !it.isFrozen }
    val frozenApps = filteredApps.filter { it.isFrozen }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (!isIslandProfileConfigured) {
            // Profile is not yet configured, show setup prompt empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RunningWithErrors,
                        contentDescription = "Setup Missing",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Island Profile Not Provisioned",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "You cannot view and manage isolated sandbox clones because a separate secure Work Profile is not configured on this device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(28.dp))
                Button(
                    onClick = onNavigateToSetup,
                    modifier = Modifier.testTag("island_navigate_setup_button")
                ) {
                    Text("Provision Island profile now")
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Search Bar header
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search isolated clones...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("clone_search_field"),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Managed profile capability advisory notice
                if (!isProfileOwner) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Status Tip",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Note: Freezing / unfreezing apps requires executing Island inside the Work Profile environment.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (filteredApps.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ContentPasteSearch,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No isolated apps matches search" else "No apps inside Island sandbox",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Go to the Mainland tab to clone installed apps here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        // Section 1: Active isolated apps
                        if (activeApps.isNotEmpty()) {
                            item {
                                Text(
                                    text = "ACTIVE ISOLATED APPS (${activeApps.size})",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
                                )
                            }
                            
                            items(activeApps, key = { "act_${it.packageName}" }) { app ->
                                val savedConfig = configs[app.packageName]
                                ClonedAppCardItem(
                                    app = app,
                                    savedConfig = savedConfig,
                                    isProfileOwner = isProfileOwner,
                                    onActionClick = {
                                        onFreezeClick(app.packageName, {}, {})
                                    },
                                    onLaunchClick = { onLaunchClick(app.packageName) },
                                    onUninstallClick = { onUninstallClick(app.packageName) },
                                    onEditConfig = {
                                        showEditDialog = app
                                        editLabel = savedConfig?.customLabel ?: ""
                                        editNotes = savedConfig?.notes ?: ""
                                        editCategory = savedConfig?.category ?: "Work"
                                        editAutoFreeze = savedConfig?.isAutoFreeze ?: false
                                    }
                                )
                            }
                        }

                        // Section 2: Frozen sandboxed apps
                        if (frozenApps.isNotEmpty()) {
                            item {
                                Text(
                                    text = "FROZEN AND SECURED (${frozenApps.size})",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp)
                                )
                            }

                            items(frozenApps, key = { "frz_${it.packageName}" }) { app ->
                                val savedConfig = configs[app.packageName]
                                ClonedAppCardItem(
                                    app = app,
                                    savedConfig = savedConfig,
                                    isProfileOwner = isProfileOwner,
                                    onActionClick = {
                                        onUnfreezeClick(app.packageName, {}, {})
                                    },
                                    onLaunchClick = {},
                                    onUninstallClick = { onUninstallClick(app.packageName) },
                                    onEditConfig = {
                                        showEditDialog = app
                                        editLabel = savedConfig?.customLabel ?: ""
                                        editNotes = savedConfig?.notes ?: ""
                                        editCategory = savedConfig?.category ?: "Work"
                                        editAutoFreeze = savedConfig?.isAutoFreeze ?: false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating refresh
        if (isIslandProfileConfigured) {
            FloatingActionButton(
                onClick = onRefresh,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .padding(bottom = 72.dp)
                    .testTag("refresh_clones_fab"),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh clones list")
            }
        }

        // Cloned app settings edit block
        if (showEditDialog != null) {
            val app = showEditDialog!!
            AlertDialog(
                onDismissRequest = { showEditDialog = null },
                title = { Text("App Workspace Config") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = editLabel,
                            onValueChange = { editLabel = it },
                            label = { Text("Custom App Name Alias") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_clone_alias"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editNotes,
                            onValueChange = { editNotes = it },
                            label = { Text("Purpose Notes") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_clone_notes"),
                            maxLines = 3
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-Freeze On Idle",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Freeze background profile services automatically.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = editAutoFreeze,
                                onCheckedChange = { editAutoFreeze = it },
                                modifier = Modifier.testTag("edit_clone_autofreeze_switch")
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onSaveConfig(
                                app.packageName,
                                if (editLabel.trim().isEmpty()) null else editLabel.trim(),
                                editCategory,
                                editNotes.trim(),
                                editAutoFreeze
                            )
                            showEditDialog = null
                        },
                        modifier = Modifier.fillMaxWidth().testTag("clone_config_save_button")
                    ) {
                        Text("Save Configuration")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showEditDialog = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    }
}

@Composable
fun ClonedAppCardItem(
    app: AppInfo,
    savedConfig: AppConfigEntity?,
    isProfileOwner: Boolean,
    onActionClick: () -> Unit,
    onLaunchClick: () -> Unit,
    onUninstallClick: () -> Unit,
    onEditConfig: () -> Unit
) {
    val displayName = savedConfig?.customLabel ?: app.appName
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("clone_item_${app.packageName}"),
        colors = CardDefaults.cardColors(
            containerColor = if (app.isFrozen) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Placeholder App icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (app.isFrozen) {
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (app.isFrozen) Icons.Default.AcUnit else Icons.Default.LocalFireDepartment,
                        contentDescription = "Status",
                        tint = if (app.isFrozen) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (app.isFrozen) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onEditConfig) {
                    Icon(
                        imageVector = Icons.Default.Edit, 
                        contentDescription = "Edit Config",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (savedConfig?.notes?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "📝 Notes: ${savedConfig.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 56.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Control Action bar on the bottom of the card
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 56.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (app.isFrozen) {
                    Button(
                        onClick = onActionClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp).testTag("action_unfreeze_${app.packageName}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Waves, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Unfreeze", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                } else {
                    Button(
                        onClick = onActionClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp).testTag("action_freeze_${app.packageName}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AcUnit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Freeze", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    OutlinedButton(
                        onClick = onLaunchClick,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp).testTag("action_launch_${app.packageName}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Launch", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onUninstallClick,
                    modifier = Modifier.size(36.dp).testTag("action_uninstall_${app.packageName}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Uninstall Clone",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
