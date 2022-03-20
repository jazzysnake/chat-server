package hu.fatbrains.routing

import hu.fatbrains.data.MessageDataSource
import hu.fatbrains.data.RoomDataSource
import hu.fatbrains.data.model.UserSession
import hu.fatbrains.plugins.AuthConfig
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

fun Route.chatRoutes(application: Application,kodein: Kodein){

    val messageDs by kodein.instance<MessageDataSource>()
    val roomDs by kodein.instance<RoomDataSource>()
    authenticate(AuthConfig.sessionAuth) {
        // Endpoint to get all messages in the room with the provided id.
        get("/message/room/{id}"){
            val id = call.parameters["id"]
            val userId = call.sessions.get<UserSession>()!!.userId
            if (id==null){
                application.log.debug("User: $userId tried to get messages from a room without providing an Id")
                call.respondText("Provide an id for the room!", status =  HttpStatusCode.BadRequest)
            }else{
                val room = roomDs.getRoomById(id)
                if (room==null){
                    application.log.debug("User: $userId tried to get messages from nonexistent room")
                    call.respondText("Room with id: $id doesn't exist", status = HttpStatusCode.BadRequest)
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
        // Endpoint to delete a message with the specified id
        post("/delete/message"){
            val id = call.receiveParameters()["id"]
            val userId = call.sessions.get<UserSession>()!!.userId
            if (id==null){
                application.log.debug("User $userId tried to delete message without providing an Id")
                call.respondText("Provide an id for the message",status= HttpStatusCode.BadRequest)
            }else{
                val message = messageDs.getMessageById(id)
                if (message!=null){
                    if (message.senderId==userId){
                        roomDs.deleteMessageFromRoom(message.roomId,id)
                        messageDs.deleteMessageById(id)
                        application.log.info("Deleted message $id")
                        call.respondText("Successfully deleted message with id: $id",status= HttpStatusCode.OK)
                    }else{
                        application.log.debug("User $userId tried to delete message $id, but is not the sender.")
                        call.respondText ("Can't delete a message sent by another user!",status= HttpStatusCode.Forbidden)
                    }
                }else{
                    application.log.debug("User $userId tried to delete nonexistent message with id $id")
                    call.respondText("Message doesn't exist with id: $id", status = HttpStatusCode.BadRequest)
                }
            }
        }
    }
}