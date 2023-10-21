package video.api.rtmpdroid.internal

import android.media.MediaFormat
import org.junit.Assert
import org.junit.Test

class ExVideoCodecsTest {
    @Test
    fun `test fromMimeTypes`() {
        val list = listOf(MediaFormat.MIMETYPE_VIDEO_HEVC)
        val videoCodecs = ExVideoCodecs.fromMimeTypes(list)
        Assert.assertTrue(videoCodecs.supportedCodecs.contains(MediaFormat.MIMETYPE_VIDEO_HEVC))
    }

    @Test
    fun `test fromMimeTypes with invalid codec`() {
        val list = listOf(MediaFormat.MIMETYPE_VIDEO_H263, MediaFormat.MIMETYPE_VIDEO_AV1)
        try {
            ExVideoCodecs.fromMimeTypes(list)
            Assert.fail("IllegalArgumentException should be thrown for H263 codec")
        } catch (_: IllegalArgumentException) {
        }
    }

    @Test
    fun `test empty fromMimeTypes`() {
        val list = emptyList<String>()
        val videoCodecs = ExVideoCodecs.fromMimeTypes(list)
        Assert.assertNull(videoCodecs.value)
        Assert.assertTrue(videoCodecs.supportedCodecs.isEmpty())
    }
}