package video.api.rtmpdroid

/**
 * Statically loads native library.
 * For internal usage only.
 */
object RtmpNativeLoader {
    init {
        System.loadLibrary("rtmpdroid")
    }
}