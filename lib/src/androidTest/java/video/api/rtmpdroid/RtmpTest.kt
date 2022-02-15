package video.api.rtmpdroid

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.net.SocketException
import java.net.SocketTimeoutException
import java.nio.ByteBuffer

/**
 * Check that methods correctly answer.
 */
class RtmpTest {
    private val rtmp = Rtmp(enableWrite = true)

    @After
    fun tearDown() {
        rtmp.close()
    }

    @Test
    fun isConnectedTest() {
        assertFalse(rtmp.isConnected)
    }

    @Test
    fun timeoutTest() {
        val timeout = 1234
        rtmp.timeout = timeout
        assertEquals(timeout, rtmp.timeout)
    }

    @Test
    fun connectTest() {
        try {
            rtmp.connect("rtmp://0.0.0.0:1935/hhs/abcde live=1")
        } catch (e: SocketException) {
        }
    }

    @Test
    fun connectStreamTest() {
        try {
            rtmp.connectStream()
        } catch (e: SocketException) {
        }
    }

    @Test
    fun deleteStreamTest() {
        try {
            rtmp.deleteStream()
        } catch (e: SocketException) {
        }
    }

    @Test
    fun readTest() {
        val data = ByteArray(10)
        try {
            rtmp.read(data)
        } catch (e: SocketTimeoutException) {
        }
    }

    @Test
    fun writeByteBufferTest() {
        val buffer = ByteBuffer.allocateDirect(10)
        try {
            rtmp.write(buffer)
        } catch (e: SocketTimeoutException) {
        }
    }

    @Test
    fun writeByteArrayTest() {
        val data = byteArrayOf(10, 20)
        try {
            rtmp.write(data)
        } catch (e: SocketTimeoutException) {
        }
    }

    @Test
    fun pauseTest() {
        try {
            rtmp.pause()
        } catch (e: SocketException) {
        }
    }

    @Test
    fun resumeTest() {
        try {
            rtmp.resume()
        } catch (e: SocketException) {
        }
    }
}