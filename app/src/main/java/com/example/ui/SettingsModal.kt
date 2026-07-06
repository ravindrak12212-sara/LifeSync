package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassmorphicCard
import com.example.viewmodel.LifeSyncViewModel

@Composable
fun SettingsModal(
    viewModel: LifeSyncViewModel,
    onDismiss: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    var usernameInput by remember(profile) { mutableStateOf(profile?.username ?: "Achiever") }
    var emailInput by remember(profile) { mutableStateOf(profile?.email ?: "user@lifesync.ai") }

    var showConfirmationDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Preferences & Identity",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_settings_button")) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close Settings")
                }
            }

            Divider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Edit Profile Section
            Text(
                text = "Profile Identity",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = usernameInput,
                onValueChange = {
                    usernameInput = it
                    viewModel.updateUsername(it)
                },
                label = { Text("Display Name") },
                leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_username_input")
            )

            OutlinedTextField(
                value = emailInput,
                onValueChange = { emailInput = it },
                label = { Text("Email Contact") },
                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Email") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false // Simulating firebase login remembers email
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Appearance Section
            Text(
                text = "Appearance Theme",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.toggleTheme() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = "Theme",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(text = "Appearance Theme", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = if (isDarkTheme) "Premium Cyber Obsidian (Dark)" else "Modern Minimal Slate (Light)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { viewModel.toggleTheme() },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Reset Section
            Text(
                text = "Data Administration",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Button(
                onClick = { showConfirmationDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("reset_profile_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "Clear")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset All Gamification Progress & Stats", fontSize = 13.sp)
            }

            // Export Simulation
            Button(
                onClick = { /* Simulated */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.CloudSync, contentDescription = "Backup")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Backup CSV/JSON (Offline)", fontSize = 13.sp)
            }
        }
    }

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text("Confirm Hard Reset") },
            text = { Text("Are you absolutely sure you want to clear your current streaks, levels, XP metrics, and coin counts back to Level 1? Your active habits templates will remain intact.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllData()
                        showConfirmationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset Progress")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
