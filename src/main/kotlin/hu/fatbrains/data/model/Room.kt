package hu.fatbrains.data.model

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@kotlinx.serialization.Serializable
data class Room(
    @BsonId
    val id:String=ObjectId().toString(),
    val name:String,
    val userIds:List<String>,
    val messageIds:List<String>,
)
