package com.yulapps.dailylockscreen.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
import com.yulapps.dailylockscreen.domain.model.UpdateLogStatus
import com.yulapps.dailylockscreen.domain.model.WallpaperMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "daily_lockscreen_settings")

class SettingsRepository(
    private val context: Context,
) {
    val settings: Flow<AppSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map(::mapSettings)

    suspend fun setAutoUpdate(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AUTO_UPDATE_KEY] = enabled
        }
    }

    suspend fun setUpdateInterval(interval: UpdateInterval) {
        context.dataStore.edit { prefs ->
            prefs[UPDATE_INTERVAL_KEY] = interval.name
        }
    }

    suspend fun setWallpaperMode(mode: WallpaperMode) {
        context.dataStore.edit { prefs ->
            prefs[WALLPAPER_MODE_KEY] = mode.name
        }
    }

    suspend fun setQuoteSourceMode(mode: QuoteSourceMode) {
        context.dataStore.edit { prefs ->
            prefs[QUOTE_SOURCE_MODE_KEY] = mode.name
        }
    }

    suspend fun setCustomQuotesInput(rawInput: String) {
        context.dataStore.edit { prefs ->
            prefs[CUSTOM_QUOTES_INPUT_KEY] = rawInput
        }
    }

    suspend fun setBackgroundImageUri(uri: String?) {
        context.dataStore.edit { prefs ->
            if (uri.isNullOrBlank()) {
                prefs.remove(BACKGROUND_IMAGE_URI_KEY)
            } else {
                prefs[BACKGROUND_IMAGE_URI_KEY] = uri
            }
        }
    }

    suspend fun setBackgroundImageScaleMode(mode: BackgroundImageScaleMode) {
        context.dataStore.edit { prefs ->
            prefs[BACKGROUND_IMAGE_SCALE_MODE_KEY] = mode.name
        }
    }

    suspend fun setBackgroundOverlayPercent(value: Int) {
        context.dataStore.edit { prefs ->
            prefs[BACKGROUND_OVERLAY_PERCENT_KEY] = value.coerceIn(0, 80)
        }
    }

    suspend fun setTextSizePreset(preset: TextSizePreset) {
        context.dataStore.edit { prefs ->
            prefs[TEXT_SIZE_PRESET_KEY] = preset.name
        }
    }

    suspend fun setTextColorPreset(preset: TextColorPreset) {
        context.dataStore.edit { prefs ->
            prefs[TEXT_COLOR_PRESET_KEY] = preset.name
        }
    }

    suspend fun setTextAlignmentPreset(preset: TextAlignmentPreset) {
        context.dataStore.edit { prefs ->
            prefs[TEXT_ALIGNMENT_PRESET_KEY] = preset.name
        }
    }

    suspend fun setTextHorizontalPosition(position: TextHorizontalPosition) {
        context.dataStore.edit { prefs ->
            prefs[TEXT_HORIZONTAL_POSITION_KEY] = position.name
        }
    }

    suspend fun setTextVerticalPosition(position: TextVerticalPosition) {
        context.dataStore.edit { prefs ->
            prefs[TEXT_VERTICAL_POSITION_KEY] = position.name
        }
    }

    suspend fun setTextFontPreset(preset: TextFontPreset) {
        context.dataStore.edit { prefs ->
            prefs[TEXT_FONT_PRESET_KEY] = preset.name
        }
    }

    suspend fun setTextBold(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[TEXT_BOLD_KEY] = enabled
        }
    }

    suspend fun setTextOpacityPercent(value: Int) {
        context.dataStore.edit { prefs ->
            prefs[TEXT_OPACITY_PERCENT_KEY] = value.coerceIn(40, 100)
        }
    }

    suspend fun setLastAppliedQuote(quote: String?) {
        context.dataStore.edit { prefs ->
            if (quote.isNullOrBlank()) {
                prefs.remove(LAST_APPLIED_QUOTE_KEY)
            } else {
                prefs[LAST_APPLIED_QUOTE_KEY] = quote
            }
        }
    }

    suspend fun setLastScheduledAt(timestampMillis: Long) {
        context.dataStore.edit { prefs ->
            prefs[LAST_SCHEDULED_AT_KEY] = timestampMillis
        }
    }

    suspend fun recordWallpaperApplied(summary: String, quote: String?) {
        val timestamp = System.currentTimeMillis()
        context.dataStore.edit { prefs ->
            prefs[LAST_UPDATED_AT_KEY] = timestamp
            prefs[LAST_APPLIED_SUMMARY_KEY] = summary
            if (quote.isNullOrBlank()) {
                prefs.remove(LAST_APPLIED_QUOTE_KEY)
            } else {
                prefs[LAST_APPLIED_QUOTE_KEY] = quote
            }
            prefs[UPDATE_LOGS_KEY] = serializeLogs(
                deserializeLogs(prefs[UPDATE_LOGS_KEY]).prepend(
                    UpdateLogEntry(
                        timestampMillis = timestamp,
                        status = UpdateLogStatus.SUCCESS,
                        message = summary,
                    ),
                ),
            )
        }
    }

    suspend fun recordWallpaperFailure(message: String) {
        recordLog(UpdateLogStatus.ERROR, message)
    }

    suspend fun recordInfo(message: String) {
        recordLog(UpdateLogStatus.INFO, message)
    }

    private suspend fun recordLog(status: UpdateLogStatus, message: String) {
        val timestamp = System.currentTimeMillis()
        context.dataStore.edit { prefs ->
            prefs[UPDATE_LOGS_KEY] = serializeLogs(
                deserializeLogs(prefs[UPDATE_LOGS_KEY]).prepend(
                    UpdateLogEntry(
                        timestampMillis = timestamp,
                        status = status,
                        message = message,
                    ),
                ),
            )
        }
    }

    private fun mapSettings(preferences: Preferences): AppSettings {
        return AppSettings(
            autoUpdate = preferences[AUTO_UPDATE_KEY] ?: false,
            updateInterval = UpdateInterval.fromStored(preferences[UPDATE_INTERVAL_KEY]),
            wallpaperMode = WallpaperMode.fromStored(preferences[WALLPAPER_MODE_KEY]),
            quoteSourceMode = QuoteSourceMode.fromStored(preferences[QUOTE_SOURCE_MODE_KEY]),
            customQuotesInput = preferences[CUSTOM_QUOTES_INPUT_KEY] ?: "",
            backgroundImageUri = preferences[BACKGROUND_IMAGE_URI_KEY],
            backgroundImageScaleMode = BackgroundImageScaleMode.fromStored(preferences[BACKGROUND_IMAGE_SCALE_MODE_KEY]),
            backgroundOverlayPercent = preferences[BACKGROUND_OVERLAY_PERCENT_KEY] ?: 42,
            textSizePreset = TextSizePreset.fromStored(preferences[TEXT_SIZE_PRESET_KEY]),
            textColorPreset = TextColorPreset.fromStored(preferences[TEXT_COLOR_PRESET_KEY]),
            textAlignmentPreset = TextAlignmentPreset.fromStored(preferences[TEXT_ALIGNMENT_PRESET_KEY]),
            textHorizontalPosition = TextHorizontalPosition.fromStored(preferences[TEXT_HORIZONTAL_POSITION_KEY]),
            textVerticalPosition = TextVerticalPosition.fromStored(preferences[TEXT_VERTICAL_POSITION_KEY]),
            textFontPreset = TextFontPreset.fromStored(preferences[TEXT_FONT_PRESET_KEY]),
            isTextBold = preferences[TEXT_BOLD_KEY] ?: false,
            textOpacityPercent = preferences[TEXT_OPACITY_PERCENT_KEY] ?: 100,
            lastAppliedQuote = preferences[LAST_APPLIED_QUOTE_KEY],
            lastAppliedSummary = preferences[LAST_APPLIED_SUMMARY_KEY],
            lastUpdatedAtMillis = preferences[LAST_UPDATED_AT_KEY],
            lastScheduledAtMillis = preferences[LAST_SCHEDULED_AT_KEY],
            updateLogs = deserializeLogs(preferences[UPDATE_LOGS_KEY]),
        )
    }

    private fun serializeLogs(logs: List<UpdateLogEntry>): String {
        val array = JSONArray()
        logs.take(MAX_LOG_ENTRIES).forEach { entry ->
            array.put(
                JSONObject()
                    .put("timestampMillis", entry.timestampMillis)
                    .put("status", entry.status.name)
                    .put("message", entry.message),
            )
        }
        return array.toString()
    }

    private fun deserializeLogs(rawValue: String?): List<UpdateLogEntry> {
        if (rawValue.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(rawValue)
            buildList {
                for (index in 0 until array.length()) {
                    val objectValue = array.getJSONObject(index)
                    add(
                        UpdateLogEntry(
                            timestampMillis = objectValue.optLong("timestampMillis"),
                            status = UpdateLogStatus.valueOf(objectValue.optString("status", UpdateLogStatus.INFO.name)),
                            message = objectValue.optString("message"),
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun List<UpdateLogEntry>.prepend(entry: UpdateLogEntry): List<UpdateLogEntry> {
        return listOf(entry) + this
    }

    private companion object {
        const val MAX_LOG_ENTRIES = 5

        val AUTO_UPDATE_KEY = booleanPreferencesKey("auto_update")
        val UPDATE_INTERVAL_KEY = stringPreferencesKey("update_interval")
        val WALLPAPER_MODE_KEY = stringPreferencesKey("wallpaper_mode")
        val QUOTE_SOURCE_MODE_KEY = stringPreferencesKey("quote_source_mode")
        val CUSTOM_QUOTES_INPUT_KEY = stringPreferencesKey("custom_quotes_input")
        val BACKGROUND_IMAGE_URI_KEY = stringPreferencesKey("background_image_uri")
        val BACKGROUND_IMAGE_SCALE_MODE_KEY = stringPreferencesKey("background_image_scale_mode")
        val BACKGROUND_OVERLAY_PERCENT_KEY = intPreferencesKey("background_overlay_percent")
        val TEXT_SIZE_PRESET_KEY = stringPreferencesKey("text_size_preset")
        val TEXT_COLOR_PRESET_KEY = stringPreferencesKey("text_color_preset")
        val TEXT_ALIGNMENT_PRESET_KEY = stringPreferencesKey("text_alignment_preset")
        val TEXT_HORIZONTAL_POSITION_KEY = stringPreferencesKey("text_horizontal_position")
        val TEXT_VERTICAL_POSITION_KEY = stringPreferencesKey("text_vertical_position")
        val TEXT_FONT_PRESET_KEY = stringPreferencesKey("text_font_preset")
        val TEXT_BOLD_KEY = booleanPreferencesKey("text_bold")
        val TEXT_OPACITY_PERCENT_KEY = intPreferencesKey("text_opacity_percent")
        val LAST_APPLIED_QUOTE_KEY = stringPreferencesKey("last_applied_quote")
        val LAST_APPLIED_SUMMARY_KEY = stringPreferencesKey("last_applied_summary")
        val LAST_UPDATED_AT_KEY = longPreferencesKey("last_updated_at")
        val LAST_SCHEDULED_AT_KEY = longPreferencesKey("last_scheduled_at")
        val UPDATE_LOGS_KEY = stringPreferencesKey("update_logs")
    }
}
