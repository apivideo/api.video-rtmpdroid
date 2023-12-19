package video.api.rtmpdroid

import video.api.rtmpdroid.internal.ExVideoCodecs
import video.api.rtmpdroid.internal.VideoCodecs
import java.io.Closeable
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.nio.ByteBuffer

/**
 * A RTMP connection.
 *
 * @param enableWrite if you are broadcasting live stream, set to [Boolean.true]. Otherwise [Boolean.false].
 */
class Rtmp(private val enableWrite: Boolean = true) : Closeable {
    companion object {
        init {
            RtmpNativeLoader
        }
    }

    private var ptr: Long

    init {
        ptr = nativeAlloc()
        if (ptr == 0L) {
            throw UnsupportedOperationException("Can't allocate a RTMP context")
        }
    }

    private external fun nativeIsConnected(): Boolean

    /**
     * Check if device it still connected with the remote RTMP server
     */
    val isConnected: Boolean
        /**
         * @return [Boolean.true] if connection is still on. Otherwise [Boolean.false].
         */
        get() = nativeIsConnected()

    private external fun nativeGetTimeout(): Int
    private external fun nativeSetTimeout(timeoutInMs: Int): Int

    /**
     * Set/get connection timeout in ms
     */
    var timeout: Int
        /**
         * @return connection timeout is ms
         */
        get() {
            val timeout = nativeGetTimeout()
            if (timeout < 0) {
                throw UnsupportedOperationException("Can't get timeout")
            }
            return timeout
        }
        /**
         * @param value connection timeout is ms
         */
        set(value) {
            if (nativeSetTimeout(value) != 0) {
                throw UnsupportedOperationException("Can't set timeout")
            }
        }

    private external fun nativeGetExVideoCodecs(): String?
    private external fun nativeSetExVideoCodec(exVideoCodec: String?): Int

    private external fun nativeGetVideoCodecs(): Int
    private external fun nativeSetVideoCodec(videoCodec: Int): Int

    /**
     * Set/get supported video codecs.
     * It is a list of video mime types.
     *
     * The supported video codecs will be send in the RTMP `connect` command either in the
     * `videoCodecs` for standard codecs or in the `fourCCList` for enhanced codecs.
     */
    var supportedVideoCodecs: List<String>
        get() {
            val videoCodecs = nativeGetVideoCodecs()
            if (videoCodecs <= 0) {
                throw UnsupportedOperationException("Can't get supported video codecs")
            }
            val supportedCodecs = VideoCodecs(videoCodecs)
            val supportedExCodecs = ExVideoCodecs(nativeGetExVideoCodecs())
            return supportedCodecs.supportedCodecs + supportedExCodecs.supportedCodecs
        }
        set(value) {
            if (value.isEmpty()) {
                throw IllegalArgumentException("At least one codec must be supported")
            }

            val supportedCodecs = mutableListOf<String>()
            val supportedExCodecs = mutableListOf<String>()
            value.forEach {
                if (VideoCodecs.isSupportedCodec(it)) {
                    supportedCodecs.add(it)
                } else if (ExVideoCodecs.isSupportedCodec(it)) {
                    supportedExCodecs.add(it)
                } else {
                    throw IllegalArgumentException("Unsupported codec $it")
                }
            }

            if (nativeSetVideoCodec(VideoCodecs.fromMimeTypes(supportedCodecs).value) != 0) {
                throw UnsupportedOperationException("Can't set supported video codecs")
            }

            if (nativeSetExVideoCodec(ExVideoCodecs.fromMimeTypes(supportedExCodecs).value) != 0) {
                throw UnsupportedOperationException("Can't set supported extended video codecs")
            }
        }

    private external fun nativeAlloc(): Long

    private external fun nativeSetupURL(url: String): Int
    private external fun nativeEnableWrite(): Int
    private external fun nativeConnect(): Int

    /**
     * Connects to a remote RTMP server.
     *
     * You must call [connectStream] after.
     * To set connect command description, appends name-value pairs to [url].
     *
     * @param url valid RTMP url (rtmp://myserver/s/streamKey)
     */
    fun connect(url: String) {
        if (nativeSetupURL(url) != 0) {
            throw IllegalArgumentException("Invalid RTMP URL: $url")
        }

        if (enableWrite) {
            if (nativeEnableWrite() != 0) {
                throw UnsupportedOperationException("Failed to enable write")
            }
        }

        if (nativeConnect() != 0) {
            throw ConnectException("Failed to connect")
        }
    }

    private external fun nativeConnectStream(): Int

    /**
     * Creates a new stream.
     *
     * @see [deleteStream]
     */
    fun connectStream() {
        if (nativeConnectStream() != 0) {
            throw ConnectException("Failed to connectStream")
        }
    }

    private external fun nativeDeleteStream(): Int

    /**
     * Deletes a running stream.
     *
     * @see [connectStream]
     */
    fun deleteStream() {
        if (nativeDeleteStream() != 0) {
            throw UnsupportedOperationException("Failed to delete stream")
        }
    }

    private external fun nativeWrite(buffer: ByteBuffer, offset: Int, size: Int): Int

    /**
     * Sends a FLV packet inside a [ByteBuffer].
     *
     * @param buffer a direct [ByteBuffer]
     * @return number of bytes sent
     */
    fun write(buffer: ByteBuffer): Int {
        require(buffer.isDirect) { "ByteBuffer must be a direct buffer" }

        val byteSent = synchronized(this) {
            nativeWrite(buffer, buffer.position(), buffer.remaining())
        }
        when {
            byteSent < 0 -> {
                throw SocketException("Connection error")
            }

            byteSent == 0 -> {
                throw SocketTimeoutException("Timeout exception")
            }

            else -> return byteSent
        }
    }

    private external fun nativeWrite(
        data: ByteArray, offset: Int, size: Int
    ): Int

    /**
     * Sends a FLV packet inside a [ByteArray].
     *
     * @param array a [ByteArray]
     * @return number of bytes sent
     */
    fun write(array: ByteArray, offset: Int = 0, size: Int = array.size): Int {
        val byteSent = synchronized(this) {
            nativeWrite(array, offset, size)
        }
        when {
            byteSent < 0 -> {
                throw SocketException("Connection error")
            }

            byteSent == 0 -> {
                throw SocketTimeoutException("Timeout exception")
            }

            else -> return byteSent
        }
    }

    private external fun nativeRead(data: ByteArray, offset: Int, size: Int): Int

    /**
     * Reads FLV packets.
     *
     * @param array a [ByteArray] where to read incoming data
     * @return number of bytes received
     */
    fun read(array: ByteArray, offset: Int = 0, size: Int = array.size): Int {
        val byteReceived = nativeRead(array, offset, size)
        when {
            byteReceived < 0 -> {
                throw SocketException("Connection error")
            }

            byteReceived == 0 -> {
                throw SocketTimeoutException("Timeout exception")
            }

            else -> return byteReceived
        }
    }

    private external fun nativeWritePacket(packet: RtmpPacket): Int

    /**
     * Write a RTMP packet
     *
     * @param packet RTMP packet to send
     * @see [readPacket]
     */
    fun writePacket(packet: RtmpPacket) {
        if (nativeWritePacket(packet) != 0) {
            throw SocketException("Failed to write packet")
        }
    }

    private external fun nativeReadPacket(): RtmpPacket?

    /**
     * Read a RTMP packet
     *
     * @return received RTMP packet
     * @see [writePacket]
     */
    fun readPacket(): RtmpPacket {
        val rtmpPacket = nativeReadPacket()
        if (rtmpPacket == null) {
            throw SocketException("Failed to read packet")
        } else {
            return rtmpPacket
        }
    }

    private external fun nativePause(): Int

    /**
     * Pauses a stream
     *
     * @see [resume]
     */
    fun pause() {
        if (nativePause() != 0) {
            throw SocketException("Can't pause")
        }
    }

    private external fun nativeResume(): Int

    /**
     * Resumes a stream after a [pause].
     *
     * @see [pause]
     */
    fun resume() {
        if (nativeResume() != 0) {
            throw SocketException("Can't resume")
        }
    }

    private external fun nativeClose()

    /**
     * Closes the RTMP connection.
     */
    override fun close() {
        if (ptr != 0L) {
            nativeClose()
            ptr = 0L
        }
    }

    private external fun nativeServe(fd: Int): Int

    /**
     * Handshakes incoming client.
     *
     * For server only.
     *
     * @param fd file descriptor of a UNIX socket.
     */
    fun serve(fd: Int) {
        if (nativeServe(fd) != 0) {
            throw SocketException("Can't serve")
        }
    }
}