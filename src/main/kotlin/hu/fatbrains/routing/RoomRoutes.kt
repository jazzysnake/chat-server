package hu.fatbrains.routing

import UserDataSource
import hu.fatbrains.data.RoomDataSource
import hu.fatbrains.data.model.Room
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

fun Route.roomRoutes(application: Application,kodein: Kodein){
    val userDs by kodein.instance<UserDataSource>()
    val roomDs by kodein.instance<RoomDataSource>()
    authenticate(AuthConfig.sessionAuth) {
        // Endpoint to create room
        // member ids are comma separated
        post("/create/room"){
            val params = call.receiveParameters()
            val creatorId = params["creator"]
            val memberIds = params["members"]
            val roomname = params["roomname"].toString()
            if(creatorId!=null&&memberIds!=null){
                val creator = userDs.getUserById(creatorId)
                val members = userDs.getUsersByIds(memberIds.split(","))
                if(creator!=null&&members.isNotEmpty()){
                    val roomMemberIds = (members+creator).map { it.id }
                    val room = Room(name = roomname, userIds = roomMemberIds, messageIds = listOf())
                    roomDs.createRoom(room)
                    application.log.info("Room ${room.id} created by user: ${creator.id}")
                    call.respondText("Created room $roomname", status = HttpStatusCode.OK)
                }else{
                    application.log.debug("Failed to create room with params: $params")
                    call.respondText("Invalid parameters", status = HttpStatusCode.BadRequest)
                }
            }else{
                application.log.debug("Tried to create room with invalid params: $params")
                call.respondText("Provide all params! (roomname,creator,members)", status = HttpStatusCode.BadRequest)
            }
        }
        // Endpoint to get all rooms belonging to the logged-in user
        get("/rooms") { // session can't be null, or the route wouldn't be accessible
            call.respond(roomDs.getRoomsByMemberId(call.sessions.get<UserSession>()!!.userId.toString()))
        }
        // Endpoint to leave the room with the logged-in user
        get("/leave/room"){
            val id = call.parameters["id"]
            // session can't be null, or the route wouldn't be accessible
            val userId = call.sessions.get<UserSession>()!!.userId.toString()
            if (id!=null){
                var room = roomDs.getRoomById(id)
                if (room!=null){
                    val remainingUsers =room.userIds.filterNot { it==userId}
                    if(remainingUsers.size > 1){
                        room = room.copy(userIds= remainingUsers)
                        roomDs.updateRoom(room)
                        application.log.info("Left room: $id with user: $userId")
                    } else{
                        roomDs.deleteRoom(id)
                        application.log.info("Left room: $id with user: $userId")
                        application.log.info("Only 1 user left in room: $id, room deleted")
                    }
                    call.respondText("Left room ${room.name}", status = HttpStatusCode.OK)
                }else{
                    application.log.debug("Tried to leave room: $id, but room doesn't exist")
                    call.respondText("Room not found", status = HttpStatusCode.BadRequest)
                }
            }else {
                application.log.debug("Requested /leave/room/{id} with no id")
                call.respondText("Provide a room id", status = HttpStatusCode.NotFound)
            }
        }
        // Endpoint to join the room with the provided id.(id param in get req)
        get("/join/room") {
            val id = call.parameters["id"]
            val userid = call.sessions.get<UserSession>()!!.userId!!
            if (id==null){
                application.log.debug("User: $userid tried to join room without specifying its id")
                call.respondText("Specify room id!", status = HttpStatusCode.BadRequest)
            }else{
                val room = roomDs.getRoomById(id)
                if (room==null){
                    application.log.debug("User: $userid tried to join nonexistent room")
                    call.respondText("Room with id: $id doesn't exist!", status = HttpStatusCode.BadRequest)
                }else{
                    if (room.userIds.contains(userid)){
                        application.log.debug("User: $userid tried to join room: ${room.id}, but is already a member.")
                        call.respondText("Already a member of room: ${room.id}", status = HttpStatusCode.BadRequest)
                    }else{
                        val modifiedRoom=room.copy(userIds = (room.userIds+userid))
                        roomDs.updateRoom(modifiedRoom)
                        application.log.info("User: $userid joined room: ${modifiedRoom.id}")
                        call.respondText("Successfully joined room ${modifiedRoom.id}", status = HttpStatusCode.OK)
                    }
                }
            }
        }
    }
}