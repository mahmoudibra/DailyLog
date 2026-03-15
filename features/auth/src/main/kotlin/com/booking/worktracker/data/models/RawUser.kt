package com.booking.worktracker.data.models

data class RawUser(
    val id: Long,
    val email: String,
    val displayName: String,
    val passwordHash: String,
    val salt: String
)
