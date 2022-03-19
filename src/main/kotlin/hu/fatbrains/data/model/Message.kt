package hu.fatbrains.data.model

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Message(
    @BsonId
    val id:String= ObjectId().toString(),
    val content:String,
    val type:MessageType,
    val timestamp:Long,
)