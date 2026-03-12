package com.booking.worktracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.booking.worktracker.data.models.ExportFormat
import com.booking.worktracker.data.repository.ExportRepository
import com.booking.worktracker.presentation.viewmodels.ExportViewModel
import com.booking.worktracker.ui.designsystem.components.*
import com.booking.worktracker.ui.designsystem.tokens.SpacingTokens
import com.booking.worktracker.ui.localization.LocalStrings
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    exportRepository: ExportRepository
) {
    val strings = LocalStrings.current
    val viewModel = remember { ExportViewModel(exportRepository) }
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val format by viewModel.format.collectAsState()
    val includeEntries by viewModel.includeEntries.collectAsState()
    val includeTags by viewModel.includeTags.collectAsState()
    val includeObjectives by viewModel.includeObjectives.collectAsState()
    val preview by viewModel.preview.collectAsState()
    val message by viewModel.message.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()

    LaunchedEffect(message) {
        if (message != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpacingTokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sectionSpacing)
    ) {
        // Header
        DSScreenTitle(strings.exportData)

        // Message banner
        message?.let { msg ->
            DSInfoBanner(
                title = strings.status,
                message = msg,
                icon = { Icon(Icons.Default.Info, contentDescription = null) }
            )
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.medium)
        ) {
            // Date range
            DSCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.medium)) {
                    DSSectionHeader(title = strings.dateRange)
                    Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.medium)) {
                        DSOutlinedTextField(
                            value = startDate,
                            onValueChange = { viewModel.setStartDate(it) },
                            label = strings.startDate,
                            placeholder = strings.dateFormatPlaceholder,
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        DSOutlinedTextField(
                            value = endDate,
                            onValueChange = { viewModel.setEndDate(it) },
                            label = strings.endDate,
                            placeholder = strings.dateFormatPlaceholder,
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Include options
            DSCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.small)) {
                    DSSectionHeader(title = strings.include)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = includeEntries, onCheckedChange = { viewModel.toggleEntries() })
                        Text(strings.workEntries, style = MaterialTheme.typography.bodyLarge)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = includeTags, onCheckedChange = { viewModel.toggleTags() })
                        Text(strings.tags, style = MaterialTheme.typography.bodyLarge)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = includeObjectives, onCheckedChange = { viewModel.toggleObjectives() })
                        Text(strings.objectives, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            // Format selector
            DSCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.small)) {
                    DSSectionHeader(title = strings.format)
                    Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.medium)) {
                        ExportFormat.values().forEach { fmt ->
                            FilterChip(
                                selected = format == fmt,
                                onClick = { viewModel.setFormat(fmt) },
                                label = {
                                    Text(
                                        when (fmt) {
                                            ExportFormat.PLAIN_TEXT -> strings.plainText
                                            ExportFormat.CSV -> strings.csv
                                            ExportFormat.MARKDOWN -> strings.markdown
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.medium)) {
                DSButton(
                    text = strings.preview,
                    icon = Icons.Default.Visibility,
                    onClick = { viewModel.generatePreview() }
                )
                DSButton(
                    text = if (isExporting) strings.exporting else strings.exportToFile,
                    icon = Icons.Default.SaveAlt,
                    enabled = !isExporting,
                    onClick = {
                        val ext = viewModel.getFileExtension()
                        val chooser = JFileChooser().apply {
                            dialogTitle = strings.exportDialogTitle
                            selectedFile = File("work_tracker_export.$ext")
                            fileFilter = FileNameExtensionFilter("${ext.uppercase()} files", ext)
                        }
                        val result = chooser.showSaveDialog(null)
                        if (result == JFileChooser.APPROVE_OPTION) {
                            viewModel.exportToFile(chooser.selectedFile.absolutePath)
                        }
                    }
                )
            }

            // Preview
            preview?.let { content ->
                DSCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.small)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DSSectionHeader(title = strings.preview)
                            DSTextButton(text = strings.close, onClick = { viewModel.clearPreview() })
                        }
                        DSDivider()
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
