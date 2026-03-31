package com.yulapps.dailylockscreen.domain.model

enum class BackgroundImageScaleMode(val label: String) {
    FIT("Fit"),
    CROP("Crop"),
    FILL("Fill"),
    ;

    companion object {
        fun fromStored(value: String?): BackgroundImageScaleMode {
            return entries.firstOrNull { it.name == value } ?: CROP
        }
    }
}
