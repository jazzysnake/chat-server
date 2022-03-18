package hu.fatbrains

import hu.fatbrains.plugins.*
import io.ktor.application.*
import hu.fatbrains.di.kodein

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    configureSerialization()
    configureSession()
    configureAuthentication(kodein)
    configureRouting(kodein)
    configureSockets()
}
