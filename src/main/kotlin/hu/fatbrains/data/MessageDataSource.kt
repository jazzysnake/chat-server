package hu.fatbrains.data

import hu.fatbrains.data.model.Message

interface MessageDataSource {
    suspend fun insertMessage(message: Message)
    suspend fun getMessagesByIds(ids:List<String>):List<Message>
    suspend fun getMessagesByRoomId(id:String):List<Message>
    suspend fun updateMessage(message: Message)
    suspend fun deleteMessageById(id: String)
}