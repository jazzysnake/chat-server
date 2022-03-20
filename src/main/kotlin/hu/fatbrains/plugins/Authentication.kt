package hu.fatbrains.plugins

import UserDataSource
import hu.fatbrains.data.model.UserSession
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.response.*
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

object AuthConfig{
    const val sessionAuth = "auth_session"
}

fun Application.configureAuthentication(kodein: Kodein){
    val userDs by kodein.instance<UserDataSource>()
    install(Authentication) {
        session<UserSession>(AuthConfig.sessionAuth) {
            // Configure session authentication
            validate { session ->
                if(session.userId==null){
                    log.info("Validation failed for Sid ${session.sessionId}")
                    null
                }
                else {
                    val user = userDs.getUserById(session.userId)
                    if (user!=null){
                        log.info("Validated User: ${session.userId}")
                        session
                    }
                    else {
                        log.debug("Tried to validate nonexistent user with id: ${session.userId}")
                        null
                    }
                }
            }
            challenge {
                call.respondRedirect("/")
            }
        }
    }
}