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
            call.respondText("Provide a email and a password!")
        }
        else if (user==null){
            call.respondText("No user registered with email: $email")
        }
        else if (BCrypt.checkpw(password,user.password)) {
            val session = call.sessions.get<UserSession>()
            if (session == null) {
                application.log.error("Session is null on route: /login")
                call.respond(HttpStatusCode.ExpectationFailed, "NO SESSION")
            } else {
                call.sessions.set(session.copy(userId = user.id))
                call.respondText("Welcome ${user.name}")
            }
        }else call.respondText("Bad password")

    }
    post("/register") {
        val params = call.receiveParameters()
        val username = params["username"]
        val email = params["email"]
        val password = params["password"]
        if (username==null||password==null||email==null){
            call.respondText("Provide a username a password and a valid email")
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
                call.respondText("Registered user: $username")
            }
            else{
                call.respondText("$email is already registered")
            }
        }
    }
    get("/logout"){
        call.sessions.clear<UserSession>()
        call.respondText("Goodbye")
    }

}