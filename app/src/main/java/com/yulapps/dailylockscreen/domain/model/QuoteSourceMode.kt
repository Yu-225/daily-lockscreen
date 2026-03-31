package com.yulapps.dailylockscreen.domain.model

enum class QuoteSourceMode(val label: String) {
    BUILT_IN("Built-in quotes"),
    CUSTOM_ONLY("Only my quotes"),
    MIXED("Mixed mode"),
    ;

    companion object {
        fun fromStored(value: String?): QuoteSourceMode {
            return entries.firstOrNull { it.name == value } ?: MIXED
        }
    }
}
