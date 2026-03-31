package com.yulapps.dailylockscreen.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.work.WorkInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yulapps.dailylockscreen.core.DisplaySizeResolver
import com.yulapps.dailylockscreen.data.QuoteRepository
import com.yulapps.dailylockscreen.data.SettingsRepository
import com.yulapps.dailylockscreen.data.WallpaperApplier
import com.yulapps.dailylockscreen.data.WallpaperGenerator
import com.yulapps.dailylockscreen.data.WallpaperScheduler
import com.yulapps.dailylockscreen.domain.model.AppSettings
import com.yulapps.dailylockscreen.domain.model.BackgroundImageScaleMode
import com.yulapps.dailylockscreen.domain.model.QuoteSourceMode
import com.yulapps.dailylockscreen.domain.model.TextAlignmentPreset
import com.yulapps.dailylockscreen.domain.model.TextColorPreset
import com.yulapps.dailylockscreen.domain.model.TextFontPreset
import com.yulapps.dailylockscreen.domain.model.TextHorizontalPosition
import com.yulapps.dailylockscreen.domain.model.TextSizePreset
import com.yulapps.dailylockscreen.domain.model.TextVerticalPosition
import com.yulapps.dailylockscreen.domain.model.UpdateInterval
import com.yulapps.dailylockscreen.domain.model.UpdateLogEntry
import com.yulapps.dailylockscreen.domain.model.WallpaperMode
import com.yulapps.dailylockscreen.domain.usecase.ApplyWallpaperUseCase
import com.yulapps.dailylockscreen.domain.usecase.GenerateWallpaperUseCase
import com.yulapps.dailylockscreen.domain.usecase.GetRandomQuoteUseCase
import com.yulapps.dailylockscreen.domain.usecase.ScheduleWallpaperUpdateUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class MainUiState(
    val isLoading: Boolean = true,
    val isWorking: Boolean = false,
    val autoUpdate: Boolean = false,
    val updateInterval: UpdateInterval = UpdateInterval.FOUR_HOURS,
    val wallpaperMode: WallpaperMode = WallpaperMode.BLACK_WITH_QUOTE,
    val quoteSourceMode: QuoteSourceMode = QuoteSourceMode.MIXED,
    val customQuotesInput: String = "",
    val customQuotesCount: Int = 0,
    val builtInQuotesCount: Int = QuoteRepository.DEFAULT_QUOTES.size,
    val quickQuoteInput: String = "",
    val backgroundImageUri: String? = null,
    val backgroundImageScaleMode: BackgroundImageScaleMode = BackgroundImageScaleMode.CROP,
    val backgroundOverlayPercent: Int = 42,
    val textSizePreset: TextSizePreset = TextSizePreset.MEDIUM,
    val textColorPreset: TextColorPreset = TextColorPreset.WHITE,
    val textAlignmentPreset: TextAlignmentPreset = TextAlignmentPreset.CENTER,
    val textHorizontalPosition: TextHorizontalPosition = TextHorizontalPosition.CENTER,
    val textVerticalPosition: TextVerticalPosition = TextVerticalPosition.CENTER,
    val textFontPreset: TextFontPreset = TextFontPreset.SANS,
    val isTextBold: Boolean = false,
    val textOpacityPercent: Int = 100,
    val previewBitmap: Bitmap? = null,
    val previewQuote: String? = null,
    val previewSummary: String = "",
    val workerStateLabel: String = "Paused",
    val lastAppliedSummary: String? = null,
    val lastUpdatedAtMillis: Long? = null,
    val nextEligibleUpdateAtMillis: Long? = null,
    val updateLogs: List<UpdateLogEntry> = emptyList(),
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val quoteRepository = QuoteRepository()
    private val settingsRepository = SettingsRepository(application)
    private val wallpaperScheduler = WallpaperScheduler(application)
    private val generateWallpaperUseCase = GenerateWallpaperUseCase(
        displaySizeResolver = DisplaySizeResolver(),
        wallpaperGenerator = WallpaperGenerator(),
        getRandomQuoteUseCase = GetRandomQuoteUseCase(quoteRepository),
    )
    private val applyWallpaperUseCase = ApplyWallpaperUseCase(
        settingsRepository = settingsRepository,
        wallpaperApplier = WallpaperApplier(application),
        generateWallpaperUseCase = generateWallpaperUseCase,
    )
    private val scheduleWallpaperUpdateUseCase = ScheduleWallpaperUpdateUseCase(
        settingsRepository = settingsRepository,
        wallpaperScheduler = wallpaperScheduler,
    )

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages = _messages.asSharedFlow()

    private var lastPreviewKey: String? = null

    init {
        observeSettings()
        observeWorkerState()
    }

    fun onAutoUpdateChanged(enabled: Boolean) {
        _uiState.update { it.copy(autoUpdate = enabled) }
        viewModelScope.launch {
            settingsRepository.setAutoUpdate(enabled)
            scheduleWallpaperUpdateUseCase.sync()
        }
    }

    fun onUpdateIntervalSelected(interval: UpdateInterval) {
        _uiState.update { it.copy(updateInterval = interval) }
        viewModelScope.launch {
            settingsRepository.setUpdateInterval(interval)
            scheduleWallpaperUpdateUseCase.sync()
        }
    }

    fun onWallpaperModeSelected(mode: WallpaperMode) {
        _uiState.update { it.copy(wallpaperMode = mode) }
        viewModelScope.launch {
            settingsRepository.setWallpaperMode(mode)
        }
    }

    fun onQuoteSourceModeSelected(mode: QuoteSourceMode) {
        _uiState.update { it.copy(quoteSourceMode = mode) }
        viewModelScope.launch {
            settingsRepository.setQuoteSourceMode(mode)
        }
    }

    fun onCustomQuotesInputChanged(input: String) {
        _uiState.update {
            it.copy(
                customQuotesInput = input,
                customQuotesCount = quoteRepository.extractCustomQuotes(input).size,
            )
        }
        viewModelScope.launch {
            settingsRepository.setCustomQuotesInput(input)
        }
    }

    fun onQuickQuoteInputChanged(input: String) {
        _uiState.update { it.copy(quickQuoteInput = input) }
    }

    fun addQuickQuote() {
        val quickQuote = _uiState.value.quickQuoteInput.trim()
        if (quickQuote.isBlank()) {
            _messages.tryEmit("Type a quote before adding it.")
            return
        }
        val merged = quoteRepository.appendQuote(_uiState.value.customQuotesInput, quickQuote)
        _uiState.update {
            it.copy(
                customQuotesInput = merged,
                customQuotesCount = quoteRepository.extractCustomQuotes(merged).size,
                quickQuoteInput = "",
            )
        }
        viewModelScope.launch {
            settingsRepository.setCustomQuotesInput(merged)
            _messages.emit("Quote added to your custom set.")
        }
    }

    fun cleanupQuotes() {
        val sanitized = quoteRepository.sanitizeCustomQuoteInput(_uiState.value.customQuotesInput)
        _uiState.update {
            it.copy(
                customQuotesInput = sanitized,
                customQuotesCount = quoteRepository.extractCustomQuotes(sanitized).size,
            )
        }
        viewModelScope.launch {
            settingsRepository.setCustomQuotesInput(sanitized)
            _messages.emit("Custom quote list cleaned up.")
        }
    }

    fun onTextSizeSelected(preset: TextSizePreset) = updateSetting(
        stateUpdate = { it.copy(textSizePreset = preset) },
        persist = { settingsRepository.setTextSizePreset(preset) },
    )

    fun onTextColorSelected(preset: TextColorPreset) = updateSetting(
        stateUpdate = { it.copy(textColorPreset = preset) },
        persist = { settingsRepository.setTextColorPreset(preset) },
    )

    fun onTextAlignmentSelected(preset: TextAlignmentPreset) = updateSetting(
        stateUpdate = { it.copy(textAlignmentPreset = preset) },
        persist = { settingsRepository.setTextAlignmentPreset(preset) },
    )

    fun onTextHorizontalPositionSelected(position: TextHorizontalPosition) = updateSetting(
        stateUpdate = { it.copy(textHorizontalPosition = position) },
        persist = { settingsRepository.setTextHorizontalPosition(position) },
    )

    fun onTextVerticalPositionSelected(position: TextVerticalPosition) = updateSetting(
        stateUpdate = { it.copy(textVerticalPosition = position) },
        persist = { settingsRepository.setTextVerticalPosition(position) },
    )

    fun onTextFontSelected(preset: TextFontPreset) = updateSetting(
        stateUpdate = { it.copy(textFontPreset = preset) },
        persist = { settingsRepository.setTextFontPreset(preset) },
    )

    fun onTextBoldChanged(enabled: Boolean) = updateSetting(
        stateUpdate = { it.copy(isTextBold = enabled) },
        persist = { settingsRepository.setTextBold(enabled) },
    )

    fun onTextOpacityChanged(value: Int) = updateSetting(
        stateUpdate = { it.copy(textOpacityPercent = value) },
        persist = { settingsRepository.setTextOpacityPercent(value) },
    )

    fun onBackgroundImageScaleModeSelected(mode: BackgroundImageScaleMode) = updateSetting(
        stateUpdate = { it.copy(backgroundImageScaleMode = mode) },
        persist = { settingsRepository.setBackgroundImageScaleMode(mode) },
    )

    fun onBackgroundOverlayChanged(value: Int) = updateSetting(
        stateUpdate = { it.copy(backgroundOverlayPercent = value) },
        persist = { settingsRepository.setBackgroundOverlayPercent(value) },
    )

    fun onBackgroundImageSelected(uriString: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true) }
            val persisted = settingsRepository.settings.first()
            val candidate = buildWorkingSettings(persisted).copy(backgroundImageUri = uriString)
            val result = runCatching {
                generateWallpaperUseCase.generate(
                    context = getApplication(),
                    settings = candidate,
                )
            }

            result.onSuccess { generated ->
                settingsRepository.setBackgroundImageUri(uriString)
                _uiState.update {
                    it.copy(
                        isWorking = false,
                        backgroundImageUri = uriString,
                        previewBitmap = generated.bitmap,
                        previewQuote = generated.quote,
                        previewSummary = generated.summary,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { it.copy(isWorking = false) }
                _messages.emit(throwable.message ?: "Could not load that image.")
            }
        }
    }

    fun removeBackgroundImage() {
        _uiState.update { it.copy(backgroundImageUri = null) }
        viewModelScope.launch {
            settingsRepository.setBackgroundImageUri(null)
        }
    }

    fun refreshPreview() {
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true) }
            val result = runCatching {
                val persisted = settingsRepository.settings.first()
                generateWallpaperUseCase.generate(
                    context = getApplication(),
                    settings = buildWorkingSettings(persisted),
                )
            }

            result.onSuccess { generatedWallpaper ->
                _uiState.update {
                    it.copy(
                        isWorking = false,
                        previewBitmap = generatedWallpaper.bitmap,
                        previewQuote = generatedWallpaper.quote,
                        previewSummary = generatedWallpaper.summary,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { it.copy(isWorking = false) }
                _messages.tryEmit(throwable.message ?: "Failed to generate preview.")
            }
        }
    }

    fun applyNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true) }
            val persisted = settingsRepository.settings.first()
            val workingSettings = buildWorkingSettings(persisted)
            val preferredQuote = if (workingSettings.wallpaperMode == WallpaperMode.BLACK_WITH_QUOTE) {
                _uiState.value.previewQuote
            } else {
                null
            }

            val result = applyWallpaperUseCase.execute(
                context = getApplication(),
                preferredQuote = preferredQuote,
                settingsOverride = workingSettings,
            )

            result.onSuccess {
                _messages.emit("Lock screen wallpaper updated.")
                refreshPreview()
            }.onFailure { throwable ->
                _uiState.update { it.copy(isWorking = false) }
                _messages.emit(throwable.message ?: "Failed to apply wallpaper.")
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.settings.collectLatest { settings ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        autoUpdate = settings.autoUpdate,
                        updateInterval = settings.updateInterval,
                        wallpaperMode = settings.wallpaperMode,
                        quoteSourceMode = settings.quoteSourceMode,
                        customQuotesInput = settings.customQuotesInput,
                        customQuotesCount = settings.customQuotes.size,
                        backgroundImageUri = settings.backgroundImageUri,
                        backgroundImageScaleMode = settings.backgroundImageScaleMode,
                        backgroundOverlayPercent = settings.backgroundOverlayPercent,
                        textSizePreset = settings.textSizePreset,
                        textColorPreset = settings.textColorPreset,
                        textAlignmentPreset = settings.textAlignmentPreset,
                        textHorizontalPosition = settings.textHorizontalPosition,
                        textVerticalPosition = settings.textVerticalPosition,
                        textFontPreset = settings.textFontPreset,
                        isTextBold = settings.isTextBold,
                        textOpacityPercent = settings.textOpacityPercent,
                        lastAppliedSummary = settings.lastAppliedSummary,
                        lastUpdatedAtMillis = settings.lastUpdatedAtMillis,
                        nextEligibleUpdateAtMillis = calculateNextEligibleUpdate(settings),
                        updateLogs = settings.updateLogs,
                    )
                }

                val previewKey = buildPreviewKey(settings)
                if (_uiState.value.previewBitmap == null || lastPreviewKey != previewKey) {
                    lastPreviewKey = previewKey
                    refreshPreview()
                }
            }
        }
    }

    private fun observeWorkerState() {
        viewModelScope.launch {
            wallpaperScheduler.observeWorkInfo().collectLatest { workInfos ->
                _uiState.update {
                    it.copy(workerStateLabel = mapWorkerState(workInfos, it.autoUpdate))
                }
            }
        }
    }

    private fun calculateNextEligibleUpdate(settings: AppSettings): Long? {
        if (!settings.autoUpdate) return null
        val anchor = settings.lastScheduledAtMillis ?: settings.lastUpdatedAtMillis ?: return null
        return anchor + settings.updateInterval.timeUnit.toMillis(settings.updateInterval.repeatInterval)
    }

    private fun buildPreviewKey(settings: AppSettings): String {
        return listOf(
            settings.wallpaperMode.name,
            settings.quoteSourceMode.name,
            settings.backgroundImageUri.orEmpty(),
            settings.backgroundImageScaleMode.name,
            settings.backgroundOverlayPercent.toString(),
            settings.textSizePreset.name,
            settings.textColorPreset.name,
            settings.textAlignmentPreset.name,
            settings.textHorizontalPosition.name,
            settings.textVerticalPosition.name,
            settings.textFontPreset.name,
            settings.isTextBold.toString(),
            settings.textOpacityPercent.toString(),
        ).joinToString(separator = "|")
    }

    private fun mapWorkerState(workInfos: List<WorkInfo>, autoUpdate: Boolean): String {
        val state = workInfos.firstOrNull()?.state ?: return if (autoUpdate) "Scheduling" else "Paused"
        return when (state) {
            WorkInfo.State.RUNNING -> "Running now"
            WorkInfo.State.ENQUEUED -> "Active"
            WorkInfo.State.BLOCKED -> "Waiting"
            WorkInfo.State.CANCELLED -> "Cancelled"
            WorkInfo.State.FAILED -> "Needs attention"
            WorkInfo.State.SUCCEEDED -> "Completed"
        }
    }

    private fun updateSetting(
        stateUpdate: (MainUiState) -> MainUiState,
        persist: suspend () -> Unit,
    ) {
        _uiState.update(stateUpdate)
        viewModelScope.launch {
            persist()
        }
    }

    private fun buildWorkingSettings(persisted: AppSettings): AppSettings {
        return persisted.copy(
            autoUpdate = _uiState.value.autoUpdate,
            updateInterval = _uiState.value.updateInterval,
            wallpaperMode = _uiState.value.wallpaperMode,
            quoteSourceMode = _uiState.value.quoteSourceMode,
            customQuotesInput = _uiState.value.customQuotesInput,
            backgroundImageUri = _uiState.value.backgroundImageUri,
            backgroundImageScaleMode = _uiState.value.backgroundImageScaleMode,
            backgroundOverlayPercent = _uiState.value.backgroundOverlayPercent,
            textSizePreset = _uiState.value.textSizePreset,
            textColorPreset = _uiState.value.textColorPreset,
            textAlignmentPreset = _uiState.value.textAlignmentPreset,
            textHorizontalPosition = _uiState.value.textHorizontalPosition,
            textVerticalPosition = _uiState.value.textVerticalPosition,
            textFontPreset = _uiState.value.textFontPreset,
            isTextBold = _uiState.value.isTextBold,
            textOpacityPercent = _uiState.value.textOpacityPercent,
        )
    }
}
