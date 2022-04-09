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
    // Endpoint to log in with credentials provided as param. (email,password)
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
                call.respondText(user.id, status = HttpStatusCode.OK)
            }
        }else {
            application.log.info("Unsuccessful login attempt by user: ${user.id}")
            call.respondText("Incorrect password", status = HttpStatusCode.Forbidden)
        }
    }
    // Endpoint to register user with the provided params. (email, username, password)
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
            val regex = Regex("^(.+)@(\\S+)$")
            if (!regex.containsMatchIn(email)){
                application.log.info("Unsuccessful user registration, invalid email provided!")
                call.respondText("$email is invalid!", status = HttpStatusCode.BadRequest)
            }
            else if(password.length<8){
                application.log.info("Unsuccessful user registration, invalid password provided!")
                call.respondText("Password must be longer than 8 characters!", status = HttpStatusCode.BadRequest)
            }
            else if(userDs.getUserByEmail(email)==null){
                val newUser= User(
                    name = username,
                    password = BCrypt.hashpw(password,BCrypt.gensalt()),
                    email = email,
                    contactIds = listOf(),
                    roomIds = listOf(),
                )
                userDs.registerUser(newUser)
                application.log.info("Successfully registered new user: $username")
                call.respondText(newUser.id, status = HttpStatusCode.OK)
            }
            else{
                application.log.info("Unsuccessful user registration, email: $email already tied to an account.")
                call.respondText("$email is already registered")
            }
        }
    }
    // Endpoint to log out
    get("/logout"){
        application.log.info("Logged out user: ${call.sessions.get<UserSession>()?.userId}")
        call.sessions.clear<UserSession>()
        call.respondText("Goodbye", status = HttpStatusCode.OK)
    }
    // Endpoint to check login validity
    get("/auth"){
        val userid = call.sessions.get<UserSession>()?.userId
        if (userid!=null)
            call.respondText("Hello $userid", status = HttpStatusCode.OK)
        else
            call.respondText("Not Logged in", status = HttpStatusCode.Forbidden)
    }
}