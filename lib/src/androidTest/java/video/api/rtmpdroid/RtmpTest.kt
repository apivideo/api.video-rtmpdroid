package video.api.rtmpdroid

import org.junit.After
import org.junit.Assert.*
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
        } catch (_: SocketException) {
        }
    }

    @Test
    fun connectStreamTest() {
        try {
            rtmp.connectStream()
        } catch (_: SocketException) {
        }
    }

    @Test
    fun deleteStreamTest() {
        try {
            rtmp.deleteStream()
        } catch (_: SocketException) {
        }
    }

    @Test
    fun readTest() {
        val data = ByteArray(10)
        try {
            rtmp.read(data)
        } catch (_: SocketTimeoutException) {
        }
    }

    @Test
    fun writeByteBufferTest() {
        val buffer = ByteBuffer.allocateDirect(10)
        try {
            rtmp.write(buffer)
        } catch (_: SocketTimeoutException) {
        }
    }

    @Test
    fun writeByteArrayTest() {
        val data = byteArrayOf(10, 20)
        try {
            rtmp.write(data)
        } catch (_: SocketTimeoutException) {
        }
    }

    @Test
    fun pauseTest() {
        try {
            rtmp.pause()
        } catch (_: SocketException) {
        }
    }

    @Test
    fun resumeTest() {
        try {
            rtmp.resume()
        } catch (_: SocketException) {
        }
    }

    @Test
    fun closeTest() {
        try {
            rtmp.close()
        } catch (_: SocketException) {
            fail("close must not throw an exception")
        }
    }

    @Test
    fun deleteStreamAfterClose() {
        try {
            rtmp.close()
            rtmp.deleteStream()
            fail("deleteStream must throw an exception if close has been called")
        } catch (_: Exception) {
        }
    }
}