package com.yulapps.dailylockscreen.domain.model

enum class WallpaperMode(val label: String) {
    BLACK_ONLY("Black only"),
    BLACK_WITH_QUOTE("Black + quote"),
    ;

    companion object {
        fun fromStored(value: String?): WallpaperMode {
            return entries.firstOrNull { it.name == value } ?: BLACK_WITH_QUOTE
        }
    }
}
