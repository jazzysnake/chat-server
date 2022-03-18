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
import kotlinx.coroutines.runBlocking
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import org.mindrot.jbcrypt.BCrypt

fun Route.authRoutes(application: Application,kodein: Kodein){
    val userDs by kodein.instance<UserDataSource>()
    post("/login"){
        runBlocking {
            val params = call.receiveParameters()
            val username = params["username"]
            val password = params["password"]
            val user = userDs.getUserByName(params["username"].toString())
            if (username==null||password==null){
                call.respondText("Provide a username and a password!")
            }
            else if (user==null){
                call.respondText("No user exists with username: ${params["username"].toString()}")
            }
            else if (BCrypt.checkpw(password,user.password)) {
                val session = call.sessions.get<UserSession>()
                if (session == null) {
                    application.log.error("Session is null on route: /login")
                    call.respond(HttpStatusCode.ExpectationFailed, "NO SESSION")
                } else {
                    call.sessions.set(session.copy(userId = user.id))
                    call.respondText("Welcome $username")
                }
            }else call.respondText("Bad password")

        }
    }
    post("/register") {
        runBlocking {
            val params = call.receiveParameters()
            val username = params["username"]
            val email = params["email"]
            val password = params["password"]
            if (username==null||password==null||email==null){
                call.respondText("Provide a username a password and a valid email")
            }
            else{
                if(userDs.getUserByName(username)==null){
                    userDs.registerUser(User(name = username, password = BCrypt.hashpw(password,BCrypt.gensalt())))
                    call.respondText("Registered user: $username")
                }
                else{
                    call.respondText("$username is already taken")
                }
            }
        }
    }

}