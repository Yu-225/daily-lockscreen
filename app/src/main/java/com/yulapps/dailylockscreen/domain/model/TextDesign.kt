package com.yulapps.dailylockscreen.domain.model

enum class TextSizePreset(
    val label: String,
    val scaleMultiplier: Float,
) {
    SMALL("Small", 0.82f),
    MEDIUM("Medium", 1.0f),
    LARGE("Large", 1.16f),
    XL("XL", 1.32f),
    ;

    companion object {
        fun fromStored(value: String?): TextSizePreset {
            return entries.firstOrNull { it.name == value } ?: MEDIUM
        }
    }
}

enum class TextColorPreset(
    val label: String,
    val argb: Long,
) {
    WHITE("White", 0xFFF5F1E8),
    ORANGE("Orange", 0xFFF2A93B),
    GRAY("Gray", 0xFFD5D0C7),
    AMBER("Amber", 0xFFD98F2B),
    ;

    companion object {
        fun fromStored(value: String?): TextColorPreset {
            return entries.firstOrNull { it.name == value } ?: WHITE
        }
    }
}

enum class TextAlignmentPreset(val label: String) {
    LEFT("Left"),
    CENTER("Center"),
    RIGHT("Right"),
    ;

    companion object {
        fun fromStored(value: String?): TextAlignmentPreset {
            return entries.firstOrNull { it.name == value } ?: CENTER
        }
    }
}

enum class TextHorizontalPosition(val label: String) {
    LEFT("Left"),
    CENTER("Center"),
    RIGHT("Right"),
    ;

    companion object {
        fun fromStored(value: String?): TextHorizontalPosition {
            return entries.firstOrNull { it.name == value } ?: CENTER
        }
    }
}

enum class TextVerticalPosition(val label: String) {
    TOP("Top"),
    CENTER("Center"),
    BOTTOM("Bottom"),
    ;

    companion object {
        fun fromStored(value: String?): TextVerticalPosition {
            return entries.firstOrNull { it.name == value } ?: CENTER
        }
    }
}

enum class TextFontPreset(val label: String) {
    SANS("Sans"),
    SERIF("Serif"),
    MONO("Mono"),
    ;

    companion object {
        fun fromStored(value: String?): TextFontPreset {
            return entries.firstOrNull { it.name == value } ?: SANS
        }
    }
}
