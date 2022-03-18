package hu.fatbrains.plugins

import UserDataSource
import hu.fatbrains.data.model.UserSession
import hu.fatbrains.routing.assignSession
import hu.fatbrains.routing.authRoutes
import hu.fatbrains.routing.roomRoutes
import hu.fatbrains.routing.userRoutes
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.runBlocking
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import org.mindrot.jbcrypt.BCrypt

fun Application.configureRouting(kodein: Kodein) {
    val userDs by kodein.instance<UserDataSource>()
    val application = this
    routing {
        assignSession(application)
        authRoutes(application,kodein)
        userRoutes(application,kodein)
        roomRoutes(application,kodein)
        get("/") {
            call.respondText("THIS WORKS")
        }
        get("/user/pista") {
            runBlocking {
                val Pista = userDs.getUserByName("Pista")
            call.respondText(Pista?.id ?: "Pista is not registered yet")
            }
        }
        authenticate(AuthConfig.sessionAuth) {
            get("/auth"){
                call.respondText("Hello ${call.sessions.get<UserSession>()?.userId}")
            }
        }

    }
}
