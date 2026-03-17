package com.booking.worktracker.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

object DispatcherProvider {
    val io: CoroutineDispatcher = Dispatchers.IO
}
