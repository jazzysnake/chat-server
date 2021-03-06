package hu.fatbrains.data

import hu.fatbrains.data.model.Room

interface RoomDataSource {
    suspend fun createRoom(room:Room)
    suspend fun getRoomById(id:String): Room?
    suspend fun getRoomsByMemberId(id:String):List<Room>
    suspend fun updateRoom(room:Room)
    suspend fun addMessageToRoom(id:String,messageId:String)
    suspend fun deleteMessageFromRoom(id:String,messageId:String)
    suspend fun deleteRoom(id: String)
}