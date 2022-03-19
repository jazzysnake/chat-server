package hu.fatbrains.data

import hu.fatbrains.data.model.Room
import org.litote.kmongo.contains
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class RoomDataSourceImpl(db:CoroutineDatabase) :RoomDataSource{
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
        //TODO delete all messages belonging to room
        rooms.deleteOneById(id)
    }
}