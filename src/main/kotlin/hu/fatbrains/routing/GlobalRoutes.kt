package hu.fatbrains.routing

import hu.fatbrains.data.model.UserSession
import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.sessions.*

fun Route.assignSession(application: Application){
    // Intercepts calls on all routes and assigns a session id to the caller
    // if the caller currently has none, or it is not valid.
    intercept(ApplicationCallPipeline.Features) {
        if (call.sessions.get<UserSession>()==null){
            val session = UserSession(sessionId = generateSessionId(), userId = null)
            call.sessions.set(session)
            application.log.info("Assigned new Session ID ${session.sessionId}")
        }
    }
}