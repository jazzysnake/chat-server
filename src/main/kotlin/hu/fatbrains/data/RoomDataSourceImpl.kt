package hu.fatbrains.data

import hu.fatbrains.data.model.Room
import org.bson.types.ObjectId
import org.litote.kmongo.contains
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.setTo

class RoomDataSourceImpl(private val db:CoroutineDatabase) :RoomDataSource{
    private val rooms=db.getCollection<Room>()

    override suspend fun createRoom(room: Room) {
        rooms.insertOne(room)
    }

    override suspend fun getRoomById(id: String): Room? {
       return rooms.findOneById(id)
    }

    override suspend fun getRoomsByMemberId(id: String): List<Room> {
       return rooms.find(Room::userIds contains id).toList()
    }

    override suspend fun updateRoom(room: Room) {
        rooms.replaceOne(Room::id eq room.id,room)
    }

    override suspend fun deleteRoom(id: String) {
       rooms.deleteOneById(id)
    }
}