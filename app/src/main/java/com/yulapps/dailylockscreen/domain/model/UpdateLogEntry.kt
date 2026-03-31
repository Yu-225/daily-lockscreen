package com.yulapps.dailylockscreen.domain.model

enum class UpdateLogStatus(val label: String) {
    SUCCESS("Success"),
    INFO("Info"),
    ERROR("Error"),
}

data class UpdateLogEntry(
    val timestampMillis: Long,
    val status: UpdateLogStatus,
    val message: String,
)
