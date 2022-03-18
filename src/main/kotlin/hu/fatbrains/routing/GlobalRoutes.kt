package hu.fatbrains.routing

import hu.fatbrains.data.model.UserSession
import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.sessions.*

fun Route.assignSession(application: Application){
    intercept(ApplicationCallPipeline.Features) {
        if (call.sessions.get<UserSession>()==null){
            val session = UserSession(sessionId = generateSessionId(), userId = "123")
            call.sessions.set(session)
            application.log.info("Assigned new Session ID ${session.sessionId}")
        }
    }
}