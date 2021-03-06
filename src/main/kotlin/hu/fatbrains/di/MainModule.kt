package hu.fatbrains.di
import UserDataSource
import hu.fatbrains.channel.ChannelStore
import hu.fatbrains.channel.ChannelStoreImpl
import hu.fatbrains.data.*
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

val kodein = Kodein {
    bind<CoroutineDatabase>() with singleton {
        val connectionString = System.getenv("MONGODB_CONNECTION")
        if (connectionString.isNotBlank()){
            KMongo.createClient(connectionString= connectionString).coroutine.getDatabase("chat_db",)
        }else{
            KMongo.createClient().coroutine.getDatabase("chat_db",)
        }
    }
    bind<UserDataSource>() with singleton {
        val db by kodein.instance<CoroutineDatabase>()
        UserDataSourceImpl(db)
    }
    bind<RoomDataSource>() with singleton {
        val db by kodein.instance<CoroutineDatabase>()
        RoomDataSourceImpl(db)
    }
    bind<MessageDataSource>() with singleton {
        val db by kodein.instance<CoroutineDatabase>()
        MessageDataSourceImpl(db)
    }
    bind<ChannelStore>() with singleton {
        ChannelStoreImpl()
    }
}