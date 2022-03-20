package hu.fatbrains.channel

import java.util.concurrent.ConcurrentHashMap

class ChannelStoreImpl :ChannelStore{
    private val channels = ConcurrentHashMap<String,Channel>()
    override suspend fun insertChannel(channel: Channel) {
        channels[channel.roomId] = channel
    }

    override suspend fun deleteChannel(channel: Channel) {
        channels.remove(channel.roomId)
    }

    override suspend fun findChannel(id: String):Channel? {
        return channels[id]
    }
}