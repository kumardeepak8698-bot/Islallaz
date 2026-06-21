package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    isIslandConfigured: Boolean,
    isRunningInIsland: Boolean,
    isProfileOwner: Boolean,
    onWipeClick: (() -> Unit, (String) -> Unit) -> Unit,
    onNavigateToSetup: () -> Unit,
    onNavigateToOnboarding: () -> Unit
) {
    var showWipeConfirmDialog by remember { mutableStateOf(false) }
    var wipeErrorMessage by remember { mutableStateOf<String?>(null) }
    var wipeSuccessMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Island Active Environment Status Dashboard
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Security Status",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sandbox Environment",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    StatusRow(
                        label = "Secure Island Configured",
                        isActive = isIslandConfigured
                    )
                    StatusRow(
                        label = "Currently Running In Island",
                        isActive = isRunningInIsland
                    )
                    StatusRow(
                        label = "Active Profile Owner License",
                        isActive = isProfileOwner
                    )
                }
            }

            // Section 2: General Sandbox Preferences Settings
            Text(
                text = "SANDBOX PREFERENCES",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Auto-Start Frozen Apps", style = MaterialTheme.typography.titleSmall)
                            Text("Start frozen apps when launcher queries action shortcut", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        var checked by remember { mutableStateOf(true) }
                        Switch(checked = checked, onCheckedChange = { checked = it })
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Draw Badge Indicator", style = MaterialTheme.typography.titleSmall)
                            Text("Draw system briefcases overlay inside cloned activities", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        var badgeChecked by remember { mutableStateOf(false) }
                        Switch(checked = badgeChecked, onCheckedChange = { badgeChecked = it })
                    }
                }
            }

            // Section 3: Onboarding & Instructions Utilities
            Text(
                text = "HELP & ONBOARDING",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(6.dp)
                ) {
                    ListItem(
                        headlineContent = { Text("Display App Tutorial") },
                        supportingContent = { Text("Review welcome onboarding pages again") },
                        leadingContent = { Icon(Icons.Default.AutoStories, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.clickable(onClick = onNavigateToOnboarding)
                    )
                    Divider()
                    ListItem(
                        headlineContent = { Text("Re-create Profiles") },
                        supportingContent = { Text("Go back to provisioning wizard setup") },
                        leadingContent = { Icon(Icons.Default.RestartAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.clickable(onClick = onNavigateToSetup)
                    )
                }
            }

            // Section 4: Critical Destruction settings
            if (isIslandConfigured) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Dangerous Action zone",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        )
                        Text(
                            text = "Wiping the Island completely deletes the managed secondary Work Profile, destroying all cloned/sandboxed applications, databases, and saved container parameters of this sandbox from this system. This operation is IRREVERSIBLE.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        
                        Button(
                            onClick = { showWipeConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("destroy_workspace_button")
                        ) {
                            Text("Destroy Island Securely", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Wipe profile confirmation dialog
        if (showWipeConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showWipeConfirmDialog = false },
                title = { Text("Confirm Workspace Destruction") },
                text = {
                    Text("Are you absolutely sure you want to permanently delete 'Island' Work Profile space? All sandboxed application cloned datas will be lost.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onWipeClick(
                                {
                                    showWipeConfirmDialog = false
                                    wipeSuccessMessage = "Profile cleared successfully"
                                },
                                { error ->
                                    showWipeConfirmDialog = false
                                    wipeErrorMessage = error
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("confirm_wipe_profile_button")
                    ) {
                        Text("Destroy Profile")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showWipeConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Show wipe success/error message
        if (wipeErrorMessage != null) {
            AlertDialog(
                onDismissRequest = { wipeErrorMessage = null },
                title = { Text("Action Required") },
                text = { Text(wipeErrorMessage!!) },
                confirmButton = {
                    Button(onClick = { wipeErrorMessage = null }) { Text("Dismiss") }
                }
            )
        }

        if (wipeSuccessMessage != null) {
            AlertDialog(
                onDismissRequest = { wipeSuccessMessage = null },
                title = { Text("Success") },
                text = { Text(wipeSuccessMessage!!) },
                confirmButton = {
                    Button(onClick = { wipeSuccessMessage = null }) { Text("OK") }
                }
            )
        }
    }
}

@Composable
fun StatusRow(
    label: String,
    isActive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (isActive) Color(0xFF10B981) else Color(0xFFEF4444),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (isActive) "Active" else "Disabled",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isActive) Color(0xFF10B981) else Color(0xFFEF4444)
            )
        }
    }
}
