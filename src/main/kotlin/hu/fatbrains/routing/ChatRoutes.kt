package hu.fatbrains.routing

import UserDataSource
import hu.fatbrains.channel.Channel
import hu.fatbrains.channel.ChannelStore
import hu.fatbrains.channel.Member
import hu.fatbrains.data.MessageDataSource
import hu.fatbrains.data.RoomDataSource
import hu.fatbrains.data.model.Message
import hu.fatbrains.data.model.MessageType
import hu.fatbrains.data.model.UserSession
import hu.fatbrains.plugins.AuthConfig
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

fun Route.chatRoutes(application: Application,kodein: Kodein){

    val messageDs by kodein.instance<MessageDataSource>()
    val roomDs by kodein.instance<RoomDataSource>()
    val channels by kodein.instance<ChannelStore>()
    val userDs by kodein.instance<UserDataSource>()
    authenticate(AuthConfig.sessionAuth) {
        // Endpoint to get all messages in the room with the provided id.
        get("/message/room"){
            val id = call.parameters["id"]
            val userId = call.sessions.get<UserSession>()!!.userId
            if (id==null){
                application.log.debug("User: $userId tried to get messages from a room without providing an Id")
                call.respondText("Provide an id for the room!")
            }else{
                val room = roomDs.getRoomById(id)
                if (room==null){
                    application.log.debug("User: $userId tried to get messages from nonexistent room")
                    call.respondText("Room with id: $id doesn't exist")
                }else{
                    if (!room.userIds.contains(userId)){
                        application.log.debug("User $userId tried to get messages from room: $id, but is not a member!")
                        call.respondText("You are not a member!", status = HttpStatusCode.Forbidden)
                    }else{
                        val messages = messageDs.getMessagesByRoomId(id)
                        application.log.info("Successfully returned ${messages.size} from room: $id, for user: $userId")
                        call.respond(HttpStatusCode.OK,messages)
                    }
                }
            }
        }

        webSocket("/chat/room/{id?}"){
            val id = call.parameters["id"]
            val userId = call.sessions.get<UserSession>()!!.userId!!
            val user = userDs.getUserById(userId)!!//TODO chechk if exist
            if (id==null){
                application.log.debug("User: $userId tried to join a chat without providing an Id")
                call.respondText("Provide an id for the chat!", status = HttpStatusCode.BadRequest)
            }else{
                val room = roomDs.getRoomById(id)
                if (room==null){
                    application.log.debug("User: $userId tried to join nonexistent chat")
                    call.respondText("Chat with id: $id doesn't exist", status = HttpStatusCode.BadRequest)
                }else{
                    if (!room.userIds.contains(userId)){
                        application.log.debug("User $userId tried to join chat: $id, but is not a member!")
                        call.respondText("You are not a member!", status = HttpStatusCode.Forbidden)
                    }else{
                        var channel =channels.findChannel(id)
                        if (channel==null){
                            channel = Channel(id,roomDs,messageDs)
                            channels.insertChannel(channel)
                        }
                        channel.onJoin(
                            Member(
                                user,
                                this
                            )
                        )
                        incoming.consumeEach {frame ->
                            when(frame){
                                is Frame.Text -> channel.sendMessage(
                                    Message(
                                        roomId = id,
                                        type = MessageType.TEXT,
                                        timestamp = System.currentTimeMillis(),
                                        content = frame.readText()
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}