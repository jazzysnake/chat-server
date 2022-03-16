package hu.fatbrains.data

import UserDataSource
import hu.fatbrains.data.model.User
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class UserDataSourceImpl(private val db:CoroutineDatabase) : UserDataSource{
    private val users = db.getCollection<User>()
    override suspend fun registerUser(user: User) {
        users.insertOne(user)
    }

    override suspend fun getUserById(id: String): User? {
        return users.findOne(User::id eq id)
    }

    override suspend fun getUserByName(name: String): User? {
        return users.findOne(User::name eq name)
    }

}