package hu.fatbrains.plugins

import UserDataSource
import hu.fatbrains.data.UserDataSourceImpl
import hu.fatbrains.data.model.User
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.runBlocking
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import org.koin.ktor.ext.inject
import org.litote.kmongo.coroutine.CoroutineDatabase

fun Application.configureRouting(kodein: Kodein) {
    val userDs by kodein.instance<UserDataSource>()
    routing {
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
