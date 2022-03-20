package hu.fatbrains.channel

interface ChannelStore {
    suspend fun insertChannel(channel: Channel)
    suspend fun deleteChannel(channel: Channel)
    suspend fun findChannel(id:String): Channel?
    suspend fun getAllChannels():List<Channel>
}