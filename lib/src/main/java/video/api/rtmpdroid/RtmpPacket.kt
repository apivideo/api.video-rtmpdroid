package video.api.rtmpdroid

import java.nio.ByteBuffer

/**
 * RTMP packet.
 * Added for test purpose only.
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

/**
 * RTMP Packet type
 * @param value RTMP int equivalent
 */
enum class PacketType(val value: Int) {

    COMMAND(0x14)
}