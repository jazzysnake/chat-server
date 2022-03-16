package hu.fatbrains

import hu.fatbrains.di.mainModule
import hu.fatbrains.plugins.*
import io.ktor.application.*
import org.koin.core.logger.Level
import org.koin.ktor.ext.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    install(Koin){
        modules(mainModule)
        slf4jLogger(Level.INFO)
    }
    configureRouting()
    configureSerialization()
    configureSockets()
}
