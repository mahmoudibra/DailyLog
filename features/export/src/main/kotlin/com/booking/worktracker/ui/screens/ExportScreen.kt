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
import com.booking.worktracker.core.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.booking.worktracker.data.models.ExportFormat
import com.booking.worktracker.presentation.viewmodels.ExportViewModel
import com.booking.worktracker.ui.designsystem.DSTheme
import com.booking.worktracker.ui.designsystem.components.*
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    viewModel: ExportViewModel
) {
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
            .padding(DSTheme.spacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.sectionSpacing)
    ) {
        // Header
        DSScreenTitle(stringResource(Res.string.export_data))

        // Message banner
        message?.let { msg ->
            DSInfoBanner(
                title = stringResource(Res.string.status),
                message = msg,
                icon = { Icon(Icons.Default.Info, contentDescription = null) }
            )
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.medium)
        ) {
            // Date range
            DSCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.medium)) {
                    DSSectionHeader(title = stringResource(Res.string.date_range))
                    Row(horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.medium)) {
                        DSOutlinedTextField(
                            value = startDate,
                            onValueChange = { viewModel.setStartDate(it) },
                            label = stringResource(Res.string.start_date),
                            placeholder = stringResource(Res.string.date_format_placeholder),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        DSOutlinedTextField(
                            value = endDate,
                            onValueChange = { viewModel.setEndDate(it) },
                            label = stringResource(Res.string.end_date),
                            placeholder = stringResource(Res.string.date_format_placeholder),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Include options
            DSCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.small)) {
                    DSSectionHeader(title = stringResource(Res.string.include))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = includeEntries, onCheckedChange = { viewModel.toggleEntries() })
                        Text(stringResource(Res.string.work_entries), style = DSTheme.font.bodyLarge)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = includeTags, onCheckedChange = { viewModel.toggleTags() })
                        Text(stringResource(Res.string.tags), style = DSTheme.font.bodyLarge)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = includeObjectives, onCheckedChange = { viewModel.toggleObjectives() })
                        Text(stringResource(Res.string.objectives), style = DSTheme.font.bodyLarge)
                    }
                }
            }

            // Format selector
            DSCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.small)) {
                    DSSectionHeader(title = stringResource(Res.string.format))
                    Row(horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.medium)) {
                        ExportFormat.values().forEach { fmt ->
                            FilterChip(
                                selected = format == fmt,
                                onClick = { viewModel.setFormat(fmt) },
                                label = {
                                    Text(
                                        when (fmt) {
                                            ExportFormat.PLAIN_TEXT -> stringResource(Res.string.plain_text)
                                            ExportFormat.CSV -> stringResource(Res.string.csv)
                                            ExportFormat.MARKDOWN -> stringResource(Res.string.markdown)
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(DSTheme.spacing.medium)) {
                DSButton(
                    text = stringResource(Res.string.action_preview),
                    icon = Icons.Default.Visibility,
                    onClick = { viewModel.generatePreview() }
                )
                val exportDialogTitle = stringResource(Res.string.export_dialog_title)
                DSButton(
                    text = if (isExporting) stringResource(Res.string.exporting) else stringResource(Res.string.export_to_file),
                    icon = Icons.Default.SaveAlt,
                    enabled = !isExporting,
                    onClick = {
                        val ext = viewModel.getFileExtension()
                        val chooser = JFileChooser().apply {
                            dialogTitle = exportDialogTitle
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
                    Column(verticalArrangement = Arrangement.spacedBy(DSTheme.spacing.small)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DSSectionHeader(title = stringResource(Res.string.action_preview))
                            DSTextButton(text = stringResource(Res.string.action_close), onClick = { viewModel.clearPreview() })
                        }
                        DSDivider()
                        Text(
                            text = content,
                            style = DSTheme.font.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
