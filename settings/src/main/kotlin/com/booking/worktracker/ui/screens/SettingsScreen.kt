package com.booking.worktracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.booking.worktracker.data.Database
import com.booking.worktracker.data.repository.SettingsRepository
import com.booking.worktracker.ui.designsystem.components.*
import com.booking.worktracker.ui.designsystem.tokens.SpacingTokens
import com.booking.worktracker.ui.localization.AppLocale
import com.booking.worktracker.ui.localization.LocalStrings
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    currentLocale: AppLocale = AppLocale.ENGLISH,
    onLanguageChanged: (AppLocale) -> Unit = {}
) {
    val strings = LocalStrings.current
    var morningTime by remember { mutableStateOf("10:30") }
    var afternoonTime by remember { mutableStateOf("16:30") }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Load current settings
    LaunchedEffect(Unit) {
        morningTime = settingsRepository.getMorningReminderTime()
        afternoonTime = settingsRepository.getAfternoonReminderTime()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpacingTokens.screenPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sectionSpacing)
    ) {
        DSScreenTitle(strings.settings)

        // Language selector card
        DSCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = strings.language,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(SpacingTokens.medium))

            AppLocale.values().forEach { locale ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentLocale == locale,
                        onClick = { onLanguageChanged(locale) }
                    )
                    Text(
                        text = "${locale.displayName} / ${locale.nativeDisplayName}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        // Reminder times card
        DSCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = strings.reminderTimes,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(SpacingTokens.large))

            // Morning reminder
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.small)
            ) {
                Text(
                    text = strings.morningReminder,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = strings.morningReminderDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                DSOutlinedTextField(
                    value = morningTime,
                    onValueChange = { morningTime = it },
                    label = strings.timeHHMM,
                    placeholder = "10:30",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.5f)
                )
            }

            Spacer(Modifier.height(SpacingTokens.large))

            // Afternoon reminder
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.small)
            ) {
                Text(
                    text = strings.afternoonReminder,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = strings.afternoonReminderDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                DSOutlinedTextField(
                    value = afternoonTime,
                    onValueChange = { afternoonTime = it },
                    label = strings.timeHHMM,
                    placeholder = "16:30",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.5f)
                )
            }
        }

        // Save button and message
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            saveMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            DSButton(
                text = strings.saveSettings,
                onClick = {
                    scope.launch {
                        try {
                            settingsRepository.setMorningReminderTime(morningTime)
                            settingsRepository.setAfternoonReminderTime(afternoonTime)
                            saveMessage = strings.settingsSaved
                            kotlinx.coroutines.delay(5000)
                            saveMessage = null
                        } catch (e: Exception) {
                            saveMessage = strings.errorMessage(e.message ?: "")
                        }
                    }
                }
            )
        }

        // Danger zone - Delete all data
        DSCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = strings.dangerZone,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(SpacingTokens.small))
            Text(
                text = strings.dangerZoneDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(SpacingTokens.medium))
            DSButton(
                text = strings.deleteAllData,
                icon = Icons.Default.DeleteForever,
                onClick = { showDeleteConfirm = true }
            )
        }

        // Info banner
        DSInfoBanner(
            title = strings.aboutReminders,
            message = strings.aboutRemindersMessage,
            icon = {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(strings.deleteAllDataConfirmTitle) },
            text = { Text(strings.deleteAllDataConfirmMessage) },
            confirmButton = {
                DSButton(
                    text = strings.deleteEverything,
                    onClick = {
                        scope.launch {
                            try {
                                Database.deleteAllData()
                                saveMessage = strings.allDataDeleted
                            } catch (e: Exception) {
                                saveMessage = strings.errorMessage(e.message ?: "")
                            }
                            showDeleteConfirm = false
                        }
                    }
                )
            },
            dismissButton = {
                DSTextButton(text = strings.cancel, onClick = { showDeleteConfirm = false })
            }
        )
    }
}
