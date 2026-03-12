package com.booking.worktracker.presentation.viewmodels

import com.booking.worktracker.data.models.ExportFormat
import com.booking.worktracker.data.models.ExportOptions
import com.booking.worktracker.data.repository.ExportRepository
import com.booking.worktracker.domain.usecases.export.GenerateExportUseCase
import com.booking.worktracker.domain.usecases.export.SaveExportToFileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.*

class ExportViewModel(
    private val repository: ExportRepository
) : ViewModel() {

    private val now = Clock.System.todayIn(TimeZone.currentSystemDefault())

    private val _startDate = MutableStateFlow(now.minus(30, DateTimeUnit.DAY).toString())
    val startDate: StateFlow<String> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow(now.toString())
    val endDate: StateFlow<String> = _endDate.asStateFlow()

    private val _format = MutableStateFlow(ExportFormat.MARKDOWN)
    val format: StateFlow<ExportFormat> = _format.asStateFlow()

    private val _includeEntries = MutableStateFlow(true)
    val includeEntries: StateFlow<Boolean> = _includeEntries.asStateFlow()

    private val _includeTags = MutableStateFlow(true)
    val includeTags: StateFlow<Boolean> = _includeTags.asStateFlow()

    private val _includeObjectives = MutableStateFlow(false)
    val includeObjectives: StateFlow<Boolean> = _includeObjectives.asStateFlow()

    private val _preview = MutableStateFlow<String?>(null)
    val preview: StateFlow<String?> = _preview.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val generateExport = GenerateExportUseCase(repository)
    private val saveExport = SaveExportToFileUseCase()

    fun setStartDate(date: String) { _startDate.value = date }
    fun setEndDate(date: String) { _endDate.value = date }
    fun setFormat(format: ExportFormat) { _format.value = format }
    fun toggleEntries() { _includeEntries.value = !_includeEntries.value }
    fun toggleTags() { _includeTags.value = !_includeTags.value }
    fun toggleObjectives() { _includeObjectives.value = !_includeObjectives.value }

    fun generatePreview() {
        viewModelScope.launch {
            val options = buildOptions()
            generateExport(options).fold(
                onSuccess = { _preview.value = it },
                onFailure = { _message.value = "Error: ${it.message}" }
            )
        }
    }

    fun exportToFile(filePath: String) {
        viewModelScope.launch {
            _isExporting.value = true
            val options = buildOptions()
            generateExport(options).fold(
                onSuccess = { content ->
                    saveExport(content, filePath).fold(
                        onSuccess = { _message.value = "Exported successfully to $filePath" },
                        onFailure = { _message.value = "Error saving file: ${it.message}" }
                    )
                },
                onFailure = { _message.value = "Error generating export: ${it.message}" }
            )
            _isExporting.value = false
        }
    }

    fun clearMessage() { _message.value = null }
    fun clearPreview() { _preview.value = null }

    fun getFileExtension(): String = when (_format.value) {
        ExportFormat.PLAIN_TEXT -> "txt"
        ExportFormat.CSV -> "csv"
        ExportFormat.MARKDOWN -> "md"
    }

    private fun buildOptions() = ExportOptions(
        startDate = _startDate.value,
        endDate = _endDate.value,
        includeEntries = _includeEntries.value,
        includeTags = _includeTags.value,
        includeObjectives = _includeObjectives.value,
        format = _format.value
    )
}
