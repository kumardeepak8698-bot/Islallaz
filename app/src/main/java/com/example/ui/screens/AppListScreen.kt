package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppConfigEntity
import com.example.model.AppInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    apps: List<AppInfo>,
    configs: Map<String, AppConfigEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onCloneClick: (String, () -> Unit, (String) -> Unit) -> Unit,
    onSaveConfig: (String, String?, String, String, Boolean) -> Unit
) {
    var selectedApp by remember { mutableStateOf<AppInfo?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }

    // State definitions inside the details edit dialog/sheet
    var customLabel by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Work") }
    var isAutoFreeze by remember { mutableStateOf(false) }

    // Filter apps based on search
    val filteredApps = apps.filter {
        it.appName.contains(searchQuery, ignoreCase = true) || 
        it.packageName.contains(searchQuery, ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Search Bar header
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { onSearchQueryChange(it) },
                placeholder = { Text("Search installed packages...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("app_search_field"),
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

            // Main List content
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
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "Empty State",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No personal apps found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        val savedConfig = configs[app.packageName]
                        val displayName = savedConfig?.customLabel ?: app.appName
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clickable {
                                    selectedApp = app
                                    customLabel = savedConfig?.customLabel ?: ""
                                    notes = savedConfig?.notes ?: ""
                                    category = savedConfig?.category ?: "Work"
                                    isAutoFreeze = savedConfig?.isAutoFreeze ?: false
                                    showDetailDialog = true
                                }
                                .testTag("app_item_${app.packageName}"),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Visual dummy app icon placeholder
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Launch,
                                        contentDescription = "App Icon",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = displayName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = app.packageName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    // Row of status badges
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text("Mainland") },
                                            modifier = Modifier.height(24.dp)
                                        )
                                        if (app.isSystem) {
                                            SuggestionChip(
                                                onClick = {},
                                                label = { Text("System") },
                                                colors = SuggestionChipDefaults.suggestionChipColors(
                                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                                ),
                                                modifier = Modifier.height(24.dp)
                                            )
                                        }
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "More details",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Float refresh button
        FloatingActionButton(
            onClick = onRefresh,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = 72.dp) // bottom nav overlap protection
                .testTag("refresh_mainland_fab"),
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh apps list")
        }

        // Application configuration & clone request details dialog
        if (showDetailDialog && selectedApp != null) {
            val app = selectedApp!!

            AlertDialog(
                onDismissRequest = { showDetailDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(text = "Sandbox Options")
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Configure custom options and clone '${app.appName}' to the isolated 'Island' user profile.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Divider()

                        OutlinedTextField(
                            value = customLabel,
                            onValueChange = { customLabel = it },
                            label = { Text("Custom App Alias (Optional)") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_custom_alias"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes / Purpose") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_app_notes"),
                            maxLines = 3
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-Freeze On Close",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Freeze app background process automatically when workspace runs idle.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isAutoFreeze,
                                onCheckedChange = { isAutoFreeze = it },
                                modifier = Modifier.testTag("edit_auto_freeze_switch")
                            )
                        }
                    }
                },
                confirmButton = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onSaveConfig(
                                    app.packageName,
                                    if (customLabel.trim().isEmpty()) null else customLabel.trim(),
                                    category,
                                    notes.trim(),
                                    isAutoFreeze
                                )
                                onCloneClick(
                                    app.packageName,
                                    { 
                                        showDetailDialog = false
                                    },
                                    { error ->
                                        // callback errors shown in parent scaffold snackbar or local
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth().testTag("dialog_clone_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AddModerator, contentDescription = "Clone icon")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Clone to Island Sandbox")
                            }
                        }
                        
                        TextButton(
                            onClick = { 
                                // Save local preferences changes only without cloning
                                onSaveConfig(
                                    app.packageName,
                                    if (customLabel.trim().isEmpty()) null else customLabel.trim(),
                                    category,
                                    notes.trim(),
                                    isAutoFreeze
                                )
                                showDetailDialog = false
                            },
                            modifier = Modifier.fillMaxWidth().testTag("dialog_save_meta_button")
                        ) {
                            Text("Save Metadata Only")
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDetailDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    }
}
