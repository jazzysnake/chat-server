package hu.fatbrains.routing

import UserDataSource
import hu.fatbrains.plugins.AuthConfig
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

fun Route.userRoutes(application: Application,kodein: Kodein){
    val userDs by kodein.instance<UserDataSource>()
    authenticate(AuthConfig.sessionAuth){
        // endpoint to get users that match either the email param, or username param.
        post("/user"){
            val params = call.receiveParameters()
            val username = params["username"].toString()
            val email = params["email"].toString()
            val users = userDs.getUsersByEmailOrName(email, username).map {
                mapOf("id" to it.id,"name" to it.name,"email" to it.email)
            }
            application.log.info("Requested users that have data containing name: $username, or email: $email")
            call.respond(users)
        }
        // endpoint to get user by id
        post("/user/{userId}"){
            val id = call.parameters["userId"]
            if (id!=null){
                val user = userDs.getUserById(id)
                application.log.info("Requested user that has id: $id")
                val userDTO= mapOf("id" to user?.id,"name" to user?.name,"email" to user?.email)
                call.respond(userDTO)
            }else{
                application.log.warn("Bad request on route /user/{userId}, no userId provided")
                call.respondText("Please provide and id",status = HttpStatusCode.BadRequest)
            }
        }
    }
}