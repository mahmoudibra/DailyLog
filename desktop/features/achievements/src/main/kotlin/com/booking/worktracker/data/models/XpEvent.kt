package com.booking.worktracker.data.models

data class XpEvent(
    val id: Int,
    val actionType: XpActionType,
    val xpAmount: Int,
    val description: String?,
    val createdAt: String
)
