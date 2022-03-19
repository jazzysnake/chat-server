import hu.fatbrains.data.model.User

interface UserDataSource {
    suspend fun registerUser(user:User)
    suspend fun getUserById(id: String):User?
    suspend fun getUserByName(name: String):User?
    suspend fun getUserByEmail(email: String):User?
    suspend fun getUsersByEmailOrName(email:String,name:String):List<User>
    suspend fun getUsersByIds(ids:List<String>):List<User>
}