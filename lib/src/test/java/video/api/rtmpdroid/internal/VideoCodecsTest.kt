package video.api.rtmpdroid.internal

import android.media.MediaFormat
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class VideoCodecsTest {
    @Test
    fun `test fromMimeTypes`() {
        val list = listOf(MediaFormat.MIMETYPE_VIDEO_H263)
        val videoCodecs = VideoCodecs.fromMimeTypes(list)
        assertTrue(videoCodecs.value == 0x4)
        assertTrue(videoCodecs.supportedCodecs.contains(MediaFormat.MIMETYPE_VIDEO_H263))
    }

    @Test
    fun `test fromMimeTypes with invalid codec`() {
        val list = listOf(MediaFormat.MIMETYPE_VIDEO_H263, MediaFormat.MIMETYPE_VIDEO_AV1)
        try {
            VideoCodecs.fromMimeTypes(list)
            fail("IllegalArgumentException should be thrown for AV1 codec")
        } catch (_: IllegalArgumentException) {
        }
    }

    @Test
    fun `test empty fromMimeTypes`() {
        val list = emptyList<String>()
        val videoCodecs = VideoCodecs.fromMimeTypes(list)
        assertTrue(videoCodecs.value == 0)
        assertTrue(videoCodecs.supportedCodecs.isEmpty())
    }
}