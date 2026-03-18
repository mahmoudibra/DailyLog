package com.dailytracker.server

import com.dailytracker.server.plugins.configureAuthentication
import com.dailytracker.server.plugins.configureCallLogging
import com.dailytracker.server.plugins.configureDatabase
import com.dailytracker.server.plugins.configureRouting
import com.dailytracker.server.plugins.configureSerialization
import com.dailytracker.server.plugins.configureStatusPages
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    configureCallLogging()
    configureSerialization()
    configureStatusPages()
    configureDatabase()
    configureAuthentication()
    configureRouting()
}
