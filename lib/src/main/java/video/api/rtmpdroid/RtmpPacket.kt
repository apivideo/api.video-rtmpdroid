package video.api.rtmpdroid

import java.io.Closeable
import java.nio.ByteBuffer

/**
 * RTMP packet.
 * Added for test purpose.
 */
class RtmpPacket(
    val channel: Int,
    val headerType: Int,
    val packetType: Int,
    val timestamp: Int,
    val buffer: ByteBuffer
) {
    constructor(
        channel: Int,
        headerType: Int,
        packetType: PacketType,
        timestamp: Int,
        buffer: ByteBuffer
    ) : this(channel, headerType, packetType.value, timestamp, buffer)
}

enum class PacketType(val value: Int) {
    COMMAND(0x14)
}