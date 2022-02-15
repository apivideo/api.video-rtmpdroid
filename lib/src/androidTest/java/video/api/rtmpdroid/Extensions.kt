package video.api.rtmpdroid

import java.nio.ByteBuffer

fun Double.toByteArray(): ByteArray = ByteBuffer.allocate(8).putDouble(this).array()

/**
 * Returns [ByteBuffer] array even if [ByteBuffer.hasArray] returns false.
 *
 * @return [ByteArray] extracted from [ByteBuffer]
 */
fun ByteBuffer.extractArray(): ByteArray {
    return if (this.hasArray()) {
        this.array()
    } else {
        val byteArray = ByteArray(this.remaining())
        this.get(byteArray)
        byteArray
    }
}