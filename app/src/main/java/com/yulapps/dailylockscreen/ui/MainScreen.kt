package com.yulapps.dailylockscreen.ui

import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.yulapps.dailylockscreen.domain.model.UpdateLogStatus
import com.yulapps.dailylockscreen.domain.model.WallpaperMode
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class AppTab(
    val title: String,
    val subtitle: String,
) {
    HOME("Home", "Preview, status and quick actions"),
    QUOTES("Quotes", "Built-in, custom and mixed quote sets"),
    DESIGN("Design", "Text styling and background controls"),
    SCHEDULE("Schedule", "Intervals, worker state and recent activity"),
    ABOUT("About", "How Daily Lockscreen works on Android"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.HOME) }
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            viewModel.onBackgroundImageSelected(uri.toString())
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.messages.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val backgroundBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF080809),
                Color(0xFF121217),
                Color(0xFF0B0C10),
            ),
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(selectedTab.title, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = selectedTab.subtitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            ) {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            DotMarker(
                                active = selectedTab == tab,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                        label = { Text(tab.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        ),
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            when (selectedTab) {
                AppTab.HOME -> HomeScreen(
                    uiState = uiState,
                    onPreview = viewModel::refreshPreview,
                    onApplyNow = viewModel::applyNow,
                )
                AppTab.QUOTES -> QuotesScreen(
                    uiState = uiState,
                    onQuoteSourceSelected = viewModel::onQuoteSourceModeSelected,
                    onQuickQuoteChanged = viewModel::onQuickQuoteInputChanged,
                    onAddQuickQuote = viewModel::addQuickQuote,
                    onCustomQuotesChanged = viewModel::onCustomQuotesInputChanged,
                    onCleanupQuotes = viewModel::cleanupQuotes,
                )
                AppTab.DESIGN -> DesignScreen(
                    uiState = uiState,
                    onWallpaperModeSelected = viewModel::onWallpaperModeSelected,
                    onTextSizeSelected = viewModel::onTextSizeSelected,
                    onTextColorSelected = viewModel::onTextColorSelected,
                    onTextAlignmentSelected = viewModel::onTextAlignmentSelected,
                    onTextHorizontalPositionSelected = viewModel::onTextHorizontalPositionSelected,
                    onTextVerticalPositionSelected = viewModel::onTextVerticalPositionSelected,
                    onTextFontSelected = viewModel::onTextFontSelected,
                    onTextBoldChanged = viewModel::onTextBoldChanged,
                    onTextOpacityChanged = viewModel::onTextOpacityChanged,
                    onPickImage = { imagePicker.launch(arrayOf("image/*")) },
                    onRemoveImage = viewModel::removeBackgroundImage,
                    onBackgroundScaleSelected = viewModel::onBackgroundImageScaleModeSelected,
                    onBackgroundOverlayChanged = viewModel::onBackgroundOverlayChanged,
                )
                AppTab.SCHEDULE -> ScheduleScreen(
                    uiState = uiState,
                    onAutoUpdateChanged = viewModel::onAutoUpdateChanged,
                    onIntervalSelected = viewModel::onUpdateIntervalSelected,
                    onPreview = viewModel::refreshPreview,
                    onApplyNow = viewModel::applyNow,
                )
                AppTab.ABOUT -> AboutScreen()
            }
        }
    }
}

@Composable
private fun HomeScreen(
    uiState: MainUiState,
    onPreview: () -> Unit,
    onApplyNow: () -> Unit,
) {
    ScreenColumn {
        HeroPreviewCard(uiState = uiState)
        StatusCard(uiState = uiState)
        SectionCard(
            eyebrow = "Quote source",
            title = when (uiState.quoteSourceMode) {
                QuoteSourceMode.BUILT_IN -> "Built-in library active"
                QuoteSourceMode.CUSTOM_ONLY -> "Using only your custom quotes"
                QuoteSourceMode.MIXED -> "Built-in and custom quotes are mixed"
            },
            subtitle = when {
                uiState.quoteSourceMode == QuoteSourceMode.CUSTOM_ONLY && uiState.customQuotesCount == 0 -> "Add your first quote in the Quotes tab to avoid empty previews."
                uiState.customQuotesCount > 0 -> "${uiState.customQuotesCount} custom quotes saved."
                else -> "${uiState.builtInQuotesCount} built-in quotes ready."
            },
        ) {
            SummaryPills(
                items = listOf(
                    "Mode: ${uiState.wallpaperMode.label}",
                    "Source: ${uiState.quoteSourceMode.label}",
                    "Custom: ${uiState.customQuotesCount}",
                ),
            )
        }
        SectionCard(
            eyebrow = "Design snapshot",
            title = if (uiState.backgroundImageUri.isNullOrBlank()) "Minimal black background" else "Custom image background",
            subtitle = "Text size ${uiState.textSizePreset.label}, ${uiState.textColorPreset.label.lowercase()}, ${uiState.textFontPreset.label.lowercase()} font.",
        ) {
            SummaryPills(
                items = listOf(
                    uiState.textAlignmentPreset.label,
                    uiState.textHorizontalPosition.label,
                    uiState.textVerticalPosition.label,
                    if (uiState.isTextBold) "Bold" else "Regular",
                    "Opacity ${uiState.textOpacityPercent}%",
                ),
            )
        }
        ActionRow(
            primaryLabel = "Apply now",
            secondaryLabel = "Refresh preview",
            primaryEnabled = !uiState.isWorking,
            secondaryEnabled = !uiState.isWorking,
            onPrimary = onApplyNow,
            onSecondary = onPreview,
        )
    }
}

@Composable
private fun QuotesScreen(
    uiState: MainUiState,
    onQuoteSourceSelected: (QuoteSourceMode) -> Unit,
    onQuickQuoteChanged: (String) -> Unit,
    onAddQuickQuote: () -> Unit,
    onCustomQuotesChanged: (String) -> Unit,
    onCleanupQuotes: () -> Unit,
) {
    ScreenColumn {
        SectionCard(
            eyebrow = "Source mode",
            title = "Choose where quotes come from",
            subtitle = "Built-in, only your own set, or a mixed pool.",
        ) {
            ChoiceRow(
                options = QuoteSourceMode.entries.toList(),
                selected = uiState.quoteSourceMode,
                label = { it.label },
                onSelected = onQuoteSourceSelected,
            )
        }
        SectionCard(
            eyebrow = "Quick add",
            title = "Add one quote fast",
            subtitle = "Great for testing the new preview without rewriting the whole set.",
        ) {
            OutlinedTextField(
                value = uiState.quickQuoteInput,
                onValueChange = onQuickQuoteChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Single quote") },
                placeholder = { Text("Write a short quote and tap Add") },
            )
            Spacer(modifier = Modifier.height(12.dp))
            FilledTonalButton(onClick = onAddQuickQuote) { Text("Add to custom set") }
        }
        SectionCard(
            eyebrow = "Custom library",
            title = "One line = one quote",
            subtitle = "Empty lines are ignored. Clean up removes blanks and duplicates.",
        ) {
            OutlinedTextField(
                value = uiState.customQuotesInput,
                onValueChange = onCustomQuotesChanged,
                modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp),
                label = { Text("Custom quotes") },
                placeholder = { Text("The morning becomes easier when your lock screen says something worth reading.") },
                minLines = 8,
                maxLines = 14,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${uiState.customQuotesCount} clean quotes available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                )
                OutlinedButton(onClick = onCleanupQuotes) { Text("Clean up list") }
            }
            if (uiState.customQuotesCount == 0) {
                Spacer(modifier = Modifier.height(12.dp))
                EmptyState(
                    title = "No custom quotes yet",
                    body = "Paste several lines here or add one quote above. Mixed mode still works with the built-in library.",
                )
            }
        }
    }
}

@Composable
private fun DesignScreen(
    uiState: MainUiState,
    onWallpaperModeSelected: (WallpaperMode) -> Unit,
    onTextSizeSelected: (TextSizePreset) -> Unit,
    onTextColorSelected: (TextColorPreset) -> Unit,
    onTextAlignmentSelected: (TextAlignmentPreset) -> Unit,
    onTextHorizontalPositionSelected: (TextHorizontalPosition) -> Unit,
    onTextVerticalPositionSelected: (TextVerticalPosition) -> Unit,
    onTextFontSelected: (TextFontPreset) -> Unit,
    onTextBoldChanged: (Boolean) -> Unit,
    onTextOpacityChanged: (Int) -> Unit,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit,
    onBackgroundScaleSelected: (BackgroundImageScaleMode) -> Unit,
    onBackgroundOverlayChanged: (Int) -> Unit,
) {
    ScreenColumn {
        SectionCard(
            eyebrow = "Wallpaper mode",
            title = "Choose what appears on the lock screen",
            subtitle = "Keep it silent with a black screen, or layer a quote on top.",
        ) {
            ChoiceRow(
                options = WallpaperMode.entries.toList(),
                selected = uiState.wallpaperMode,
                label = { it.label },
                onSelected = onWallpaperModeSelected,
            )
        }
        SectionCard(
            eyebrow = "Typography",
            title = "Tune how the quote feels",
            subtitle = "Preset controls are safer than a fully free canvas and stay predictable in the worker too.",
        ) {
            LabelBlock("Text size") {
                ChoiceRow(TextSizePreset.entries.toList(), uiState.textSizePreset, { it.label }, onTextSizeSelected)
            }
            LabelBlock("Text color") {
                ChoiceRow(TextColorPreset.entries.toList(), uiState.textColorPreset, { it.label }, onTextColorSelected)
            }
            LabelBlock("Line alignment") {
                ChoiceRow(TextAlignmentPreset.entries.toList(), uiState.textAlignmentPreset, { it.label }, onTextAlignmentSelected)
            }
            LabelBlock("Horizontal placement") {
                ChoiceRow(TextHorizontalPosition.entries.toList(), uiState.textHorizontalPosition, { it.label }, onTextHorizontalPositionSelected)
            }
            LabelBlock("Vertical placement") {
                ChoiceRow(TextVerticalPosition.entries.toList(), uiState.textVerticalPosition, { it.label }, onTextVerticalPositionSelected)
            }
            LabelBlock("Font preset") {
                ChoiceRow(TextFontPreset.entries.toList(), uiState.textFontPreset, { it.label }, onTextFontSelected)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Bold text")
                Switch(checked = uiState.isTextBold, onCheckedChange = onTextBoldChanged)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Text opacity ${uiState.textOpacityPercent}%")
            Slider(
                value = uiState.textOpacityPercent.toFloat(),
                onValueChange = { onTextOpacityChanged(it.toInt()) },
                valueRange = 40f..100f,
                steps = 5,
            )
        }
        SectionCard(
            eyebrow = "Background",
            title = if (uiState.backgroundImageUri.isNullOrBlank()) "Using a black base" else "Custom image enabled",
            subtitle = if (uiState.backgroundImageUri.isNullOrBlank()) {
                "Add a photo from the gallery and keep the quote readable with overlay controls."
            } else {
                displayUriLabel(uiState.backgroundImageUri)
            },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FilledTonalButton(onClick = onPickImage, modifier = Modifier.weight(1f)) {
                    Text(if (uiState.backgroundImageUri.isNullOrBlank()) "Pick image" else "Change image")
                }
                OutlinedButton(
                    onClick = onRemoveImage,
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.backgroundImageUri.isNullOrBlank(),
                ) { Text("Remove image") }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (uiState.backgroundImageUri.isNullOrBlank()) {
                EmptyState(
                    title = "No custom image yet",
                    body = "The lock screen still works with a clean black background. Add an image any time if you want a warmer, more poster-like feel.",
                )
            } else {
                LabelBlock("Scale mode") {
                    ChoiceRow(
                        options = BackgroundImageScaleMode.entries.toList(),
                        selected = uiState.backgroundImageScaleMode,
                        label = { it.label },
                        onSelected = onBackgroundScaleSelected,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Dark overlay ${uiState.backgroundOverlayPercent}%")
                Slider(
                    value = uiState.backgroundOverlayPercent.toFloat(),
                    onValueChange = { onBackgroundOverlayChanged(it.toInt()) },
                    valueRange = 0f..80f,
                    steps = 7,
                )
            }
        }
    }
}

@Composable
private fun ScheduleScreen(
    uiState: MainUiState,
    onAutoUpdateChanged: (Boolean) -> Unit,
    onIntervalSelected: (UpdateInterval) -> Unit,
    onPreview: () -> Unit,
    onApplyNow: () -> Unit,
) {
    ScreenColumn {
        SectionCard(
            eyebrow = "Service status",
            title = if (uiState.autoUpdate) "Background refresh is enabled" else "Background refresh is paused",
            subtitle = "WorkManager is best effort. Exact minute-perfect refresh is not guaranteed on Android.",
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusPill(
                    label = uiState.workerStateLabel,
                    tone = when (uiState.workerStateLabel) {
                        "Active", "Running now" -> MaterialTheme.colorScheme.primary
                        "Paused" -> MaterialTheme.colorScheme.outline
                        else -> MaterialTheme.colorScheme.secondary
                    },
                )
                Switch(checked = uiState.autoUpdate, onCheckedChange = onAutoUpdateChanged)
            }
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow("Last update", formatTimestamp(uiState.lastUpdatedAtMillis) ?: "Not yet")
            InfoRow("Next eligible update", formatNextEligible(uiState.nextEligibleUpdateAtMillis))
            InfoRow("Last applied", uiState.lastAppliedSummary ?: "No wallpaper applied yet")
            Spacer(modifier = Modifier.height(16.dp))
            SummaryPills(
                items = listOf(
                    "Best effort execution",
                    "Next update may drift slightly",
                    "Battery optimization can delay runs",
                ),
            )
        }
        SectionCard(
            eyebrow = "Interval",
            title = "Choose a schedule Android can keep alive",
            subtitle = "V1 uses 15 min to 24 h intervals because standard periodic work cannot go below 15 minutes.",
        ) {
            ChoiceRow(
                options = UpdateInterval.entries.toList(),
                selected = uiState.updateInterval,
                label = { it.label },
                onSelected = onIntervalSelected,
            )
        }
        SectionCard(
            eyebrow = "Recent activity",
            title = "Last wallpaper events",
            subtitle = "The most recent 3-5 updates, scheduling changes or errors are shown here.",
        ) {
            if (uiState.updateLogs.isEmpty()) {
                EmptyState(
                    title = "No activity yet",
                    body = "Apply a wallpaper manually or enable auto update to start building a visible history.",
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.updateLogs.forEachIndexed { index, log ->
                        LogRow(log = log)
                        if (index != uiState.updateLogs.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        }
                    }
                }
            }
        }
        ActionRow(
            primaryLabel = "Apply now",
            secondaryLabel = "Refresh preview",
            primaryEnabled = !uiState.isWorking,
            secondaryEnabled = !uiState.isWorking,
            onPrimary = onApplyNow,
            onSecondary = onPreview,
        )
    }
}

@Composable
private fun AboutScreen() {
    ScreenColumn {
        SectionCard(
            eyebrow = "What this app does",
            title = "Daily Lockscreen keeps your lock screen fresh",
            subtitle = "It generates wallpapers locally, stores your settings on-device and can refresh the lock screen in the background.",
        ) {
            SummaryPills(items = listOf("Local quotes and settings", "Lock screen only", "No account required"))
        }
        SectionCard(
            eyebrow = "Android reality check",
            title = "Background updates are approximate by design",
            subtitle = "Android may delay periodic work because of doze mode, OEM battery limits and normal scheduler batching.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("• Background updates are best effort.")
                Text("• Next update may drift slightly.")
                Text("• Some OEM firmware is more aggressive about background limits.")
                Text("• Choosing 15 minutes does not mean exact minute-level precision.")
            }
        }
        SectionCard(
            eyebrow = "Build info",
            title = "Version 0.1.0",
            subtitle = "Daily Lockscreen V1 foundation",
        ) {
            Text(
                text = "Adaptive launcher icon, product theme, quotes, design controls and schedule status are all now part of the app foundation.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
            )
        }
    }
}

@Composable
private fun HeroPreviewCard(uiState: MainUiState) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Current preview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = uiState.previewSummary.ifBlank { "Preview and final wallpaper use the same rendering pipeline." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
                    )
                }
                StatusPill(
                    label = if (uiState.autoUpdate) "Active" else "Manual",
                    tone = if (uiState.autoUpdate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                )
            }
            Box(
                modifier = Modifier.fillMaxWidth().height(420.dp).clip(RoundedCornerShape(28.dp)).background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                if (uiState.previewBitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = uiState.previewBitmap.asImageBitmap(),
                        contentDescription = "Wallpaper preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                if (uiState.previewBitmap == null || uiState.isLoading || uiState.isWorking) {
                    Text(
                        text = if (uiState.isWorking || uiState.isLoading) "Rendering preview..." else "Tap Preview to render the current setup",
                        color = Color.White.copy(alpha = 0.84f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(28.dp),
                    )
                }
            }
            Text(
                text = when {
                    !uiState.previewQuote.isNullOrBlank() -> uiState.previewQuote
                    uiState.wallpaperMode == WallpaperMode.BLACK_ONLY -> "Black only mode keeps the screen intentionally calm."
                    uiState.quoteSourceMode == QuoteSourceMode.CUSTOM_ONLY && uiState.customQuotesCount == 0 -> "Add your first custom quote to see text in the preview."
                    else -> "Preview uses the same layout rules as the final wallpaper, including wrapping and centering."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun StatusCard(uiState: MainUiState) {
    SectionCard(
        eyebrow = "Active status",
        title = if (uiState.autoUpdate) "Wallpaper refresh is armed" else "Manual mode is active",
        subtitle = "The app now surfaces what the worker is doing and what the system may delay.",
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatusPill(label = uiState.workerStateLabel, tone = MaterialTheme.colorScheme.primary)
            StatusPill(label = uiState.updateInterval.label, tone = MaterialTheme.colorScheme.secondary)
        }
        Spacer(modifier = Modifier.height(16.dp))
        InfoRow("Last updated", formatTimestamp(uiState.lastUpdatedAtMillis) ?: "Not yet")
        InfoRow("Next eligible", formatNextEligible(uiState.nextEligibleUpdateAtMillis))
        InfoRow("Last applied", uiState.lastAppliedSummary ?: "No wallpaper has been applied yet")
    }
}

@Composable
private fun SectionCard(
    eyebrow: String,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(eyebrow.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.76f))
            }
            content()
        }
    }
}

@Composable
private fun ActionRow(
    primaryLabel: String,
    secondaryLabel: String,
    primaryEnabled: Boolean,
    secondaryEnabled: Boolean,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = onSecondary, modifier = Modifier.weight(1f), enabled = secondaryEnabled) { Text(secondaryLabel) }
        Button(onClick = onPrimary, modifier = Modifier.weight(1f), enabled = primaryEnabled) { Text(primaryLabel) }
    }
}

@Composable
private fun SummaryPills(items: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items.forEach { item -> StatusPill(label = item, tone = MaterialTheme.colorScheme.surfaceTint) }
    }
}

@Composable
private fun StatusPill(label: String, tone: Color) {
    Row(
        modifier = Modifier.clip(CircleShape).background(tone.copy(alpha = 0.14f)).border(1.dp, tone.copy(alpha = 0.24f), CircleShape).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DotMarker(active = true, tint = tone)
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = tone)
    }
}

@Composable
private fun DotMarker(active: Boolean, tint: Color) {
    Box(modifier = Modifier.size(if (active) 10.dp else 8.dp).clip(CircleShape).background(if (active) tint else tint.copy(alpha = 0.32f)))
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f))
        Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun EmptyState(title: String, body: String) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.background.copy(alpha = 0.45f)).padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.76f))
        }
    }
}

@Composable
private fun <T> ChoiceRow(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        options.forEach { option ->
            FilterChip(selected = option == selected, onClick = { onSelected(option) }, label = { Text(label(option)) })
        }
    }
}

@Composable
private fun LabelBlock(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.76f))
        content()
    }
}

@Composable
private fun LogRow(log: UpdateLogEntry) {
    val tone = when (log.status) {
        UpdateLogStatus.SUCCESS -> MaterialTheme.colorScheme.primary
        UpdateLogStatus.INFO -> MaterialTheme.colorScheme.secondary
        UpdateLogStatus.ERROR -> MaterialTheme.colorScheme.error
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusPill(label = log.status.label, tone = tone)
            Text(formatTimestamp(log.timestampMillis) ?: "Just now", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f))
        }
        Text(log.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun ScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}

private fun formatTimestamp(timestampMillis: Long?): String? {
    if (timestampMillis == null) return null
    return DateTimeFormatter.ofPattern("HH:mm, d MMM").format(Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.systemDefault()))
}

private fun formatNextEligible(timestampMillis: Long?): String {
    if (timestampMillis == null) return "Not scheduled"
    val now = System.currentTimeMillis()
    return if (timestampMillis <= now) "Eligible now, but Android may still delay execution" else formatTimestamp(timestampMillis) ?: "Scheduled"
}

private fun displayUriLabel(uriString: String?): String {
    if (uriString.isNullOrBlank()) return "No image selected"
    return uriString.substringAfterLast('/').ifBlank { "Custom image selected" }
}

