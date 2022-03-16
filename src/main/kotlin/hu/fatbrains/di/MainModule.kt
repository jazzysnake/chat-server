package hu.fatbrains.di
import UserDataSource
import hu.fatbrains.data.UserDataSourceImpl
import org.koin.dsl.module
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

val mainModule = module {
    single {
        KMongo.createClient().coroutine.getDatabase("test_user_db")
    }
    single<UserDataSource> {
        UserDataSourceImpl(db = get())
    }
}