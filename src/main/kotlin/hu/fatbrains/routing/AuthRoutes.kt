package hu.fatbrains.routing

import UserDataSource
import hu.fatbrains.data.model.User
import hu.fatbrains.data.model.UserSession
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import org.mindrot.jbcrypt.BCrypt

fun Route.authRoutes(application: Application,kodein: Kodein){
    val userDs by kodein.instance<UserDataSource>()
    post("/login"){
        val params = call.receiveParameters()
        val email = params["email"]
        val password = params["password"]
        val user = userDs.getUserByEmail(params["email"].toString())
        if (email==null||password==null){
            application.log.debug("Failed login attempt, bad params provided. Params: $params")
            call.respondText("Provide a email and a password!", status = HttpStatusCode.BadRequest)
        }
        else if (user==null){
            application.log.debug("Failed login attempt, no user registered with provided email. Params: $params")
            call.respondText("No user registered with email: $email", status = HttpStatusCode.BadRequest)
        }
        else if (BCrypt.checkpw(password,user.password)) {
            val session = call.sessions.get<UserSession>()
            if (session == null) {
                application.log.error("Session is null on route: /login")
                call.respond(HttpStatusCode.ExpectationFailed, "NO SESSION")
            } else {
                call.sessions.set(session.copy(userId = user.id))
                application.log.info("Logged-in with user:${user.id}")
                call.respondText("Welcome ${user.name}", status = HttpStatusCode.OK)
            }
        }else {
            application.log.info("Unsuccessful login attempt by user: ${user.id}")
            call.respondText("Incorrect password")
        }

    }
    post("/register") {
        val params = call.receiveParameters()
        val username = params["username"]
        val email = params["email"]
        val password = params["password"]
        if (username==null||password==null||email==null){
            application.log.debug("Failed registration attempt, bad params provided. Params: $params")
            call.respondText("Provide a username a password and a valid email", status = HttpStatusCode.BadRequest)
        }
        else{
            if(userDs.getUserByEmail(email)==null){
                userDs.registerUser(User(
                    name = username,
                    password = BCrypt.hashpw(password,BCrypt.gensalt()),
                    email = email,
                    contactIds = listOf(),
                    roomIds = listOf(),
                ))
                application.log.info("Successfully registered new user: $username")
                call.respondText("Registered user: $username", status = HttpStatusCode.OK)
            }
            else{
                application.log.info("Unsuccessful user registration, email: $email already tied to an account.")
                call.respondText("$email is already registered")
            }
        }
    }
    get("/logout"){
        application.log.info("Logged out user: ${call.sessions.get<UserSession>()?.userId}")
        call.sessions.clear<UserSession>()
        call.respondText("Goodbye", status = HttpStatusCode.OK)
    }

}