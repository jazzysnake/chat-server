package hu.fatbrains

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import hu.fatbrains.plugins.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting()
        configureSerialization()
        configureSockets()
    }.start(wait = true)
}

fun Application.configureRouting(){
    routing {
        get("/"){
            call.respond("THIS WORKS")
        }
    }
}

fun Application.configureSockets() {
    install(WebSockets)
    routing {
        webSocket("/chat") {
            send("You are connected!")
            for(frame in incoming) {
                frame as? Frame.Text ?: continue
                val receivedText = frame.readText()
                send("You said: $receivedText")
            }
        }
    }
}
