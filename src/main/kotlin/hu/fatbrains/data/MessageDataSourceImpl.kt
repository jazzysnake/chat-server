package hu.fatbrains.data

import hu.fatbrains.data.model.Message
import org.litote.kmongo.`in`
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class MessageDataSourceImpl(db:CoroutineDatabase) :MessageDataSource{
    private val messages =db.getCollection<Message>()

    override suspend fun insertMessage(message: Message) {
        messages.insertOne(message)
    }

    override suspend fun getMessagesByIds(ids: List<String>): List<Message> {
        return messages.find(Message::id `in` ids).toList()
    }

    override suspend fun getMessagesByRoomId(id: String): List<Message> {
        return messages.find(Message::roomId eq id).toList()
    }

    override suspend fun updateMessage(message: Message) {
        messages.replaceOne(Message::id eq message.id,message)
    }

    override suspend fun deleteMessageById(id: String) {
        messages.deleteOneById(id)
    }
}