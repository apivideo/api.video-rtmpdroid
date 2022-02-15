package video.api.rtmpdroid.amf

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import video.api.rtmpdroid.amf.models.EcmaArray
import video.api.rtmpdroid.amf.models.NamedParameter
import video.api.rtmpdroid.toByteArray

/**
 * Check that methods correctly answer.
 */
class AmfEncoderTest {
    private val amfEncoder = AmfEncoder()

    @Test
    fun getBufferSizeTest() {
        amfEncoder.add(4)
        assertEquals(4, amfEncoder.minBufferSize)

        val s = "Test"
        amfEncoder.add(s)
        assertEquals(4 + 3 + s.length, amfEncoder.minBufferSize)
    }

    @Test
    fun encodeBooleanTest() {
        val b = true
        amfEncoder.add(b)
        val buffer = amfEncoder.encode()

        val expectedArray = byteArrayOf(
            AmfType.BOOLEAN.value, if (b) {
                1
            } else {
                0
            }
        )
        assertArrayEquals(expectedArray, buffer.array().sliceArray(IntRange(4, 4 + buffer.limit() - 1)))
    }

    @Test
    fun encodeIntTest() {
        val i = 4
        amfEncoder.add(i)
        val buffer = amfEncoder.encode()

        assertEquals(i, buffer.getInt(0))
    }

    @Test
    fun encodeStringTest() {
        val s = "stringToEncode"
        amfEncoder.add(s)
        val buffer = amfEncoder.encode()

        val expectedArray = byteArrayOf(AmfType.STRING.value, 0, s.length.toByte()) + s.toByteArray()
        assertArrayEquals(
            expectedArray,
            buffer.array().sliceArray(
                IntRange(
                    4,
                    4 + buffer.limit() - 1
                )
            ) // 4 bytes for direct byte buffer
        )
    }

    @Test
    fun encodeNamedBooleanTest() {
        val p = NamedParameter("myBoolean", true)
        amfEncoder.add(p)
        val buffer = amfEncoder.encode()

        val expectedArray = byteArrayOf(
            0x0,
            p.name.length.toByte()
        ) + p.name.toByteArray() + byteArrayOf(
            AmfType.BOOLEAN.value, if (p.value as Boolean) {
                1
            } else {
                0
            }
        )
        assertArrayEquals(
            expectedArray,
            buffer.array().sliceArray(IntRange(4, 4 + buffer.limit() - 1))
        ) // 4 bytes for direct byte buffer
    }

    @Test
    fun encodeNamedDoubleTest() {
        val p = NamedParameter("myNumber", 4.0)
        amfEncoder.add(p)
        val buffer = amfEncoder.encode()

        val expectedArray = byteArrayOf(
            0x0,
            p.name.length.toByte()
        ) + p.name.toByteArray() + byteArrayOf(0) + (p.value as Double).toByteArray()
        assertArrayEquals(
            expectedArray,
            buffer.array().sliceArray(IntRange(4, 4 + buffer.limit() - 1))
        ) // 4 bytes for direct byte buffer
    }

    @Test
    fun encodeNamedStringTest() {
        val p = NamedParameter("myString", "stringToEncode")
        amfEncoder.add(p)
        val buffer = amfEncoder.encode()

        val expectedArray =
            byteArrayOf(0x0, p.name.length.toByte()) + p.name.toByteArray() + byteArrayOf(
                AmfType.STRING.value,
                0,
                (p.value as String).length.toByte()
            ) + (p.value as String).toByteArray()
        assertArrayEquals(
            expectedArray,
            buffer.array().sliceArray(IntRange(4, 4 + buffer.limit() - 1))
        ) // 4 bytes for direct byte buffer
    }

    @Test
    fun encodeArrayWithIntTest() {
        val i = 4
        val a = EcmaArray()
        a.add(i)
        amfEncoder.add(a)
        val buffer = amfEncoder.encode()

        val expectedArray = byteArrayOf(
            AmfType.ECMA_ARRAY.value, 0, 0, 0, 1, // Array header
            0, 0, 0, 4, // value
            0, 0, AmfType.OBJECT_END.value // Array footer
        )
        assertArrayEquals(
            expectedArray,
            buffer.array().sliceArray(IntRange(4, 4 + buffer.limit() - 1))
        ) // 4 bytes for direct byte buffer
    }
}