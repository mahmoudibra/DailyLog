package com.booking.worktracker.ui.localization

enum class AppLocale(
    val code: String,
    val displayName: String,
    val nativeDisplayName: String,
    val isRtl: Boolean = false
) {
    ENGLISH("en", "English", "English"),
    ARABIC("ar", "Arabic", "\u0627\u0644\u0639\u0631\u0628\u064A\u0629", isRtl = true);

    companion object {
        fun fromCode(code: String): AppLocale =
            values().find { it.code == code } ?: ENGLISH
    }
}
