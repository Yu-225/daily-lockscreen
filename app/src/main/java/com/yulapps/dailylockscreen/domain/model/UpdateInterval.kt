package com.yulapps.dailylockscreen.domain.model

import java.util.concurrent.TimeUnit

enum class UpdateInterval(
    val label: String,
    val repeatInterval: Long,
    val timeUnit: TimeUnit,
) {
    FIFTEEN_MINUTES("15 min", 15, TimeUnit.MINUTES),
    THIRTY_MINUTES("30 min", 30, TimeUnit.MINUTES),
    ONE_HOUR("1 hour", 1, TimeUnit.HOURS),
    TWO_HOURS("2 hours", 2, TimeUnit.HOURS),
    FOUR_HOURS("4 hours", 4, TimeUnit.HOURS),
    SIX_HOURS("6 hours", 6, TimeUnit.HOURS),
    TWELVE_HOURS("12 hours", 12, TimeUnit.HOURS),
    TWENTY_FOUR_HOURS("24 hours", 24, TimeUnit.HOURS),
    ;

    companion object {
        fun fromStored(value: String?): UpdateInterval {
            return entries.firstOrNull { it.name == value } ?: FOUR_HOURS
        }
    }
}
