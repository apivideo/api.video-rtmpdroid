package video.api.rtmpdroid

object RtmpNativeLoader {
    init {
        System.loadLibrary("rtmpdroid")
    }
}