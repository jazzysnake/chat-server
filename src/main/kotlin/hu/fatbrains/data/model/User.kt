package hu.fatbrains.data.model

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class User(
    @BsonId
    val id:String =ObjectId().toString(),
    val img:String?,
    val name:String,
    val email:String,
    val password:String,
    val contactIds:List<String>,
    val roomIds:List<String>,
)
