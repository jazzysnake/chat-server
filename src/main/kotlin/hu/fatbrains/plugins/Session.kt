package hu.fatbrains.plugins

import hu.fatbrains.data.model.UserSession
import io.ktor.application.*
import io.ktor.sessions.*
import java.io.File

fun Application.configureSession(){
    install(Sessions) {
        cookie<UserSession>("user_session", directorySessionStorage(File(".sessions"), cached = true))
    }
}