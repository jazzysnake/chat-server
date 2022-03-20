package hu.fatbrains.channel

import hu.fatbrains.data.MessageDataSource
import hu.fatbrains.data.RoomDataSource
import hu.fatbrains.data.model.Message
import io.ktor.http.cio.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

class Channel(val roomId: String,private val roomDs: RoomDataSource,private val messageDs: MessageDataSource){
    private val members = ConcurrentHashMap<String,Member>()

    fun onJoin(member: Member){
        if (!members.containsKey(member.user.id)){
            members[member.user.id] = member
        }
    }

    suspend fun sendMessage(message: Message){
        messageDs.insertMessage(message)
        roomDs.addMessageToRoom(roomId,message.id)
        members.values.forEach{ member ->
            member.socket.send(Frame.Text(Json.encodeToString(message)))
        }
    }

    suspend fun tryDisconnect(id:String){
        members[id]?.socket?.close()
        if (members.containsKey(id))
            members.remove(id)
    }
    fun size():Int{
        return members.size
    }
}
