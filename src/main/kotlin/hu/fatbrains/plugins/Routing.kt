package hu.fatbrains.plugins

import hu.fatbrains.data.UserDataSourceImpl
import hu.fatbrains.data.model.User
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.inject
import org.litote.kmongo.coroutine.CoroutineDatabase

fun Application.configureRouting() {
    val db by inject<CoroutineDatabase>()
    val userDs = UserDataSourceImpl(db)
    routing {
        // val userDs by inject<UserDataSourceImpl>()
        get("/") {
            call.respondText("THIS WORKS")
        }
        get("/register") {
            runBlocking {
                userDs.registerUser(User(name = "Pista"))
            }
            call.respondText("Reqistered Pista")
        }
        get("/user/pista") {
            runBlocking {
                val Pista = userDs.getUserByName("Pista")
            call.respondText(Pista?.id ?: "Pista is not registered yet")
        }
        }
    }
}
