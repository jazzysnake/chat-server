package hu.fatbrains.channel

import hu.fatbrains.data.model.User
import io.ktor.http.cio.websocket.*

data class Member(
    val user: User,
    val socket:WebSocketSession
)
