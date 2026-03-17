package com.dailytracker.server.models

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val email: String
)

@Serializable
data class ErrorResponse(
    val message: String
)

@Serializable
data class SnapshotInfo(
    val id: String,
    val name: String,
    val fileSize: Long,
    val schemaVersion: Int?,
    val createdAt: String
)

@Serializable
data class SnapshotListResponse(
    val snapshots: List<SnapshotInfo>
)
