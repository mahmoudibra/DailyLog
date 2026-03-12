package com.booking.worktracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.booking.worktracker.core.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.booking.worktracker.data.Database
import com.booking.worktracker.data.repository.SettingsRepository
import com.booking.worktracker.ui.designsystem.DSTheme
import com.booking.worktracker.ui.designsystem.components.*
import com.booking.worktracker.ui.localization.AppLocale
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository = SettingsRepository(),
    currentLocale: AppLocale = AppLocale.ENGLISH,
    isDarkMode: Boolean = false,
    onLanguageChanged: (AppLocale) -> Unit = {},
    onDarkModeChanged: (Boolean) -> Unit = {}
) {
    var morningTime by remember { mutableStateOf("10:30") }
    var afternoonTime by remember { mutableStateOf("16:30") }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Capture strings needed inside coroutines
    val savedMsg = stringResource(Res.string.settings_saved)
    val deletedMsg = stringResource(Res.string.all_data_deleted)

    // Load current settings
    LaunchedEffect(Unit) {
        morningTime = settingsRepository.getMorningReminderTime()
        afternoonTime = settingsRepository.getAfternoonReminderTime()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DSTheme.spacing.screenPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.sectionSpacing)
    ) {
        DSScreenTitle(stringResource(Res.string.settings))

        // Language selector card
        DSCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(Res.string.language),
                style = DSTheme.font.titleLarge,
                color = DSTheme.colors.primary
            )

            Spacer(Modifier.height(DSTheme.spacing.medium))

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
                        style = DSTheme.font.bodyLarge
                    )
                }
            }
        }

        // Dark mode card
        DSCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DarkMode,
                        contentDescription = null,
                        tint = DSTheme.colors.primary
                    )
                    Column {
                        Text(
                            text = stringResource(Res.string.dark_mode),
                            style = DSTheme.font.titleLarge,
                            color = DSTheme.colors.primary
                        )
                        Text(
                            text = stringResource(Res.string.dark_mode_desc),
                            style = DSTheme.font.bodySmall,
                            color = DSTheme.colors.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { onDarkModeChanged(it) }
                )
            }
        }

        // Reminder times card
        DSCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(Res.string.reminder_times),
                style = DSTheme.font.titleLarge,
                color = DSTheme.colors.primary
            )

            Spacer(Modifier.height(DSTheme.spacing.large))

            // Morning reminder
            Column(
                verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.small)
            ) {
                Text(
                    text = stringResource(Res.string.morning_reminder),
                    style = DSTheme.font.titleMedium
                )
                Text(
                    text = stringResource(Res.string.morning_reminder_desc),
                    style = DSTheme.font.bodySmall,
                    color = DSTheme.colors.onSurfaceVariant
                )
                DSOutlinedTextField(
                    value = morningTime,
                    onValueChange = { morningTime = it },
                    label = stringResource(Res.string.time_hhmm),
                    placeholder = "10:30",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.5f)
                )
            }

            Spacer(Modifier.height(DSTheme.spacing.large))

            // Afternoon reminder
            Column(
                verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.small)
            ) {
                Text(
                    text = stringResource(Res.string.afternoon_reminder),
                    style = DSTheme.font.titleMedium
                )
                Text(
                    text = stringResource(Res.string.afternoon_reminder_desc),
                    style = DSTheme.font.bodySmall,
                    color = DSTheme.colors.onSurfaceVariant
                )
                DSOutlinedTextField(
                    value = afternoonTime,
                    onValueChange = { afternoonTime = it },
                    label = stringResource(Res.string.time_hhmm),
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
                    style = DSTheme.font.bodyMedium,
                    color = DSTheme.colors.primary
                )
            }

            DSButton(
                text = stringResource(Res.string.save_settings),
                onClick = {
                    scope.launch {
                        try {
                            settingsRepository.setMorningReminderTime(morningTime)
                            settingsRepository.setAfternoonReminderTime(afternoonTime)
                            saveMessage = savedMsg
                            kotlinx.coroutines.delay(5000)
                            saveMessage = null
                        } catch (e: Exception) {
                            saveMessage = "Error: ${e.message ?: ""}"
                        }
                    }
                }
            )
        }

        // Danger zone - Delete all data
        DSCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(Res.string.danger_zone),
                style = DSTheme.font.titleLarge,
                color = DSTheme.colors.error
            )
            Spacer(Modifier.height(DSTheme.spacing.small))
            Text(
                text = stringResource(Res.string.danger_zone_desc),
                style = DSTheme.font.bodySmall,
                color = DSTheme.colors.onSurfaceVariant
            )
            Spacer(Modifier.height(DSTheme.spacing.medium))
            DSButton(
                text = stringResource(Res.string.delete_all_data),
                icon = Icons.Default.DeleteForever,
                onClick = { showDeleteConfirm = true }
            )
        }

        // Info banner
        DSInfoBanner(
            title = stringResource(Res.string.about_reminders),
            message = stringResource(Res.string.about_reminders_message),
            icon = {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = DSTheme.colors.onSecondaryContainer
                )
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(Res.string.delete_all_data_confirm_title)) },
            text = { Text(stringResource(Res.string.delete_all_data_confirm_message)) },
            confirmButton = {
                DSButton(
                    text = stringResource(Res.string.delete_everything),
                    onClick = {
                        scope.launch {
                            try {
                                Database.deleteAllData()
                                saveMessage = deletedMsg
                            } catch (e: Exception) {
                                saveMessage = "Error: ${e.message ?: ""}"
                            }
                            showDeleteConfirm = false
                        }
                    }
                )
            },
            dismissButton = {
                DSTextButton(text = stringResource(Res.string.action_cancel), onClick = { showDeleteConfirm = false })
            }
        )
    }
}
