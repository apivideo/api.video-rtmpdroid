package video.api.rtmpdroid

import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import video.api.rtmpdroid.amf.AmfEncoder
import java.nio.ByteBuffer

/**
 * Check that methods correctly answer.
 */
class RtmpMessageTest {
    private val rtmp = Rtmp()
    private val rtmpServer = RtmpServer()

    companion object {
        private const val FLV_HEADER_TAG_SIZE = 11
    }

    private fun writeScriptData(payload: ByteBuffer): ByteBuffer {
        val dataSize = payload.remaining().toShort()
        val flvTagSize = FLV_HEADER_TAG_SIZE + dataSize
        val buffer =
            ByteBuffer.allocateDirect(flvTagSize + 4) // 4 - PreviousTagSize

        // FLV Tag
        buffer.put(18) // script data
        buffer.put(0)
        buffer.putShort(dataSize)
        buffer.putInt(0) // ts
        buffer.put(0) // 24 bit for Stream ID
        buffer.putShort(0) // 24 bit for Stream ID

        buffer.put(payload)

        buffer.putInt(flvTagSize)

        buffer.rewind()

        return buffer
    }

    private fun createFakeFlvBuffer(): ByteBuffer {
        val amfEncoder = AmfEncoder()
        amfEncoder.add("myField", 4.0)
        return writeScriptData(amfEncoder.encode())
    }

    private fun createFakeFlvArray(): ByteArray {
        val buffer = createFakeFlvBuffer()
        return buffer.array().sliceArray(IntRange(4, 4 + buffer.limit() - 1))
    }

    @After
    fun tearDown() {
        rtmp.close()
        rtmpServer.shutdown()
    }

    @Test
    fun connectTest() {
        val futureData = rtmpServer.enqueueConnect()
        rtmp.connect("rtmp://127.0.0.1:${rtmpServer.port}/app/playpath")
        rtmp.connectStream()
        assertEquals(true, futureData.get())
    }

    @Test
    fun writeByteArrayTest() {
        val expectedArray = createFakeFlvArray()
        val futureData = rtmpServer.enqueueRead()
        rtmp.connect("rtmp://127.0.0.1:${rtmpServer.port}/app/playpath")
        rtmp.connectStream()
        rtmp.write(expectedArray)
        val resultBuffer = futureData.get()
        assertArrayEquals(
            expectedArray.sliceArray(IntRange(11, expectedArray.size - 5)),
            resultBuffer.extractArray().sliceArray(IntRange(16, resultBuffer.limit() - 1))
        )
    }

    @Test
    fun writeByteBufferTest() {
        val expectedBuffer = createFakeFlvBuffer()
        expectedBuffer.rewind()
        val futureData = rtmpServer.enqueueRead()
        rtmp.connect("rtmp://127.0.0.1:${rtmpServer.port}/app/playpath")
        rtmp.connectStream()
        rtmp.write(expectedBuffer)
        val resultBuffer = futureData.get()
        assertArrayEquals(
            expectedBuffer.extractArray().sliceArray(IntRange(15, expectedBuffer.limit() - 1)),
            resultBuffer.extractArray().sliceArray(IntRange(16, resultBuffer.limit() - 1))
        )
    }
}