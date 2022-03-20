package hu.fatbrains.plugins

import hu.fatbrains.routing.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kodein.di.Kodein

fun Application.configureRouting(kodein: Kodein) {
    val application = this
    routing {
        assignSession(application)
        get("/") {
            call.respondText("Chat server made by FatBrains")
        }
        authRoutes(application,kodein)
        userRoutes(application,kodein)
        roomRoutes(application,kodein)
        chatRoutes(application,kodein)
        socketRoutes(application,kodein)
    }
}
