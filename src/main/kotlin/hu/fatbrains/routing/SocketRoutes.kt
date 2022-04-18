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

fun Route.socketRoutes(application: Application,kodein: Kodein){
    val messageDs by kodein.instance<MessageDataSource>()
    val roomDs by kodein.instance<RoomDataSource>()
    val channels by kodein.instance<ChannelStore>()
    val userDs by kodein.instance<UserDataSource>()
    authenticate(AuthConfig.sessionAuth) {
        webSocket("/chat/room/{id}"){
            application.log.info(this.toString())
            val id = call.parameters["id"]
            val userId = call.sessions.get<UserSession>()!!.userId!!
            val user = userDs.getUserById(userId)!!
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
                            application.log.info("Creating new channel for room: $id")
                            application.log.info("Current number of channels: ${channels.getAllChannels().size}")
                        }
                        try {
                            channel.onJoin(
                                Member(
                                    user,
                                    this
                                )
                            )
                            incoming.consumeEach {frame ->
                                if (frame is Frame.Text){
                                    application.log.info("Sending text message")
                                    channel.sendMessage(
                                        Message(
                                            senderId= userId,
                                            roomId = id,
                                            type = MessageType.TEXT,
                                            timestamp = System.currentTimeMillis(),
                                            content = frame.readText()
                                        )
                                    )
                                }
                                else if (frame is Frame.Binary){
                                    application.log.info("Sending binary message")
                                    channel.sendMessage(
                                        Message(
                                            senderId= userId,
                                            roomId = id,
                                            type = MessageType.BINARY,
                                            timestamp = System.currentTimeMillis(),
                                            content = java.util.Base64.getEncoder().encodeToString(frame.data),
                                        )
                                    )
                                }
                            }
                        }catch (e: java.lang.Exception){
                            application.log.error(e.toString())
                        }finally {
                            channel.tryDisconnect(userId)
                            application.log.info("User: $userId disconnected from channel: $channel")
                            application.log.info("Remaining channel size: ${channel.size()}")
                            if (channel.size()<1){
                                channels.deleteChannel(channel)
                                application.log.info("Deleting channel $channel")
                                application.log.info("Current number of channels: ${channels.getAllChannels().size}")
                            }
                        }
                    }
                }
            }

        }

    }
}