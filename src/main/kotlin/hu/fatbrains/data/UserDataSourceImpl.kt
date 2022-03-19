package hu.fatbrains.data

import UserDataSource
import com.mongodb.client.model.Filters.or
import hu.fatbrains.data.model.User
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.regex

class UserDataSourceImpl(db:CoroutineDatabase) : UserDataSource{
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

    override suspend fun getUserByEmail(email: String): User? {
        return users.findOne(User::email eq email)
    }

    override suspend fun getUsersByEmailOrName(email: String, name: String): List<User> {
        if (email.isNotBlank()&&name.isNotBlank())
            return users.find(or(User::email regex "${email}.*".toRegex(),User::email regex "${name}.*".toRegex())).toList()
        if ( email.isNotBlank())
            return users.find(User::email regex "${email}.*".toRegex()).toList()
        if (name.isNotBlank())
            return users.find(User::email regex "${name}.*".toRegex()).toList()
        return listOf()
    }

    override suspend fun getUsersByIds(ids: List<String>): List<User?> {
        return ids.map{
            users.findOne(User::id eq it)
        }
    }

}