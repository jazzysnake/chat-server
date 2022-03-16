import hu.fatbrains.data.model.User

interface UserDataSource {
    suspend fun registerUser(user:User)
    suspend fun getUserById(id: String):User?
    suspend fun getUserByName(name: String):User?
}