package hu.fatbrains.data.model

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@kotlinx.serialization.Serializable
data class Message(
    @BsonId
    val id:String= ObjectId().toString(),
    val senderId:String,
    val roomId:String,
    val content:String,
    val type:MessageType,
    val timestamp:Long,
)