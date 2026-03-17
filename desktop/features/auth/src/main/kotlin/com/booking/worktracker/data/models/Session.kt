package com.booking.worktracker.data.models

data class Session(
    val id: Long,
    val userId: Long,
    val token: String,
    val expiresAt: String,
    val createdAt: String
)
