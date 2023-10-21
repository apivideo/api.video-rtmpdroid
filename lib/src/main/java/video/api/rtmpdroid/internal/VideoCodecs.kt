package video.api.rtmpdroid.internal

import android.media.MediaFormat
import android.os.Build

/**
 * Handler for video codecs supported by RTMP protocol.
 * It uses to map `videoCodecs` from `connect` command to mime types.
 *
 * For codecs described in enhanced RTMP, see [ExVideoCodecs]
 *
 * @param value supported video codecs
 */
class VideoCodecs(val value: Int) {
    /**
     * The mime type of the codecs contains in the [value] of `videoCodec`.
     */
    val supportedCodecs = run {
        val list = mutableListOf<String>()
        for ((mimeType, codec) in codecsMap) {
            if (value and codec != 0) {
                list.add(mimeType)
            }
        }
        list
    }

    /**
     * Whether the value has the codec.
     *
     * @param mimeType video codec mime type
     * @return true if codec is supported, otherwise false
     */
    fun hasCodec(mimeType: String): Boolean {
        return supportedCodecs.contains(mimeType)
    }

    companion object {
        private const val SUPPORT_VID_UNUSED = 0x0001
        private const val SUPPORT_VID_JPEG = 0x0002
        private const val SUPPORT_VID_SORENSON = 0x0004
        private const val SUPPORT_VID_HOMEBREW = 0x0008
        private const val SUPPORT_VID_VP6 = 0x0010
        private const val SUPPORT_VID_VP6ALPHA = 0x0020
        private const val SUPPORT_VID_HOMEBREWV = 0x0040
        private const val SUPPORT_VID_H264 = 0x0080

        private val codecsMap = mapOf(
            MediaFormat.MIMETYPE_VIDEO_H263 to SUPPORT_VID_SORENSON,
            MediaFormat.MIMETYPE_VIDEO_AVC to SUPPORT_VID_H264
        )

        /**
         * Whether the mime type is a RTMP supported codec.
         *
         * @param mimeType video codec mime type
         * @return true if codec is supported by RTMP, otherwise false
         */
        fun isSupportedCodec(mimeType: String): Boolean {
            return codecsMap.containsKey(mimeType)
        }

        /**
         * Creates a [VideoCodecs] from a list of mime types.
         *
         * @param mimeTypes list of mime types
         * @return [VideoCodecs] instance
         */
        fun fromMimeTypes(mimeTypes: List<String>): VideoCodecs {
            var value = 0
            for (mimeType in mimeTypes) {
                val codec = codecsMap[mimeType]
                if (codec != null) {
                    value = value or codec
                } else {
                    throw IllegalArgumentException("Mimetype $mimeType is not supported by RTMP protocol")
                }
            }
            return VideoCodecs(value)
        }
    }
}

/**
 * Handler for video codecs supported by enhanced RTMP protocol.
 *
 * @param value supported video codecs with format `hvc1[,av01][,vp09]`
 */
class ExVideoCodecs(val value: String?) {
    private val fourCCList = value?.split(",")?.toList() ?: emptyList()

    init {
        for (mimeType in supportedCodecs) {
            if (!isSupportedCodec(mimeType)) {
                throw IllegalArgumentException("Mimetype $mimeType is not supported by enhanced RTMP protocol")
            }
        }
    }

    /**
     * The mime type of the codecs contains in the [value] of `fourCcList`.
     */
    val supportedCodecs: List<String>
        get() {
            val list = mutableListOf<String>()
            for ((mimeType, fourCC) in codecsMap) {
                if (fourCCList.contains(fourCC)) {
                    list.add(mimeType)
                }
            }
            return list
        }

    /**
     * Whether the value has the codec.
     *
     * @param mimeType video codec mime type
     * @return true if codec is supported, otherwise false
     */
    fun hasCodec(mimeType: String): Boolean {
        return supportedCodecs.contains(mimeType)
    }

    companion object {
        private const val AV1_FOURCC_TAG = "av01"
        private const val VP9_FOURCC_TAG = "vp9"
        private const val HEVC_FOURCC_TAG = "hvc1"

        private val codecsMap = mutableMapOf(
            MediaFormat.MIMETYPE_VIDEO_VP9 to VP9_FOURCC_TAG,
            MediaFormat.MIMETYPE_VIDEO_HEVC to HEVC_FOURCC_TAG
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                this[MediaFormat.MIMETYPE_VIDEO_AV1] = AV1_FOURCC_TAG
            }
        }.toMap()

        /**
         * Whether the mime type is a RTMP supported extended codec.
         *
         * @param mimeType video codec mime type
         * @return true if codec is supported by enhanced RTMP, otherwise false
         */
        fun isSupportedCodec(mimeType: String): Boolean {
            return codecsMap.containsKey(mimeType)
        }

        /**
         * Creates a [ExVideoCodecs] from a list of mime types.
         */
        fun fromMimeTypes(mimeTypes: List<String>): ExVideoCodecs {
            if (mimeTypes.isEmpty()) {
                return ExVideoCodecs(null)
            }
            val value = mutableListOf<String>()
            for (mimeType in mimeTypes) {
                val codec = codecsMap[mimeType]
                if (codec != null) {
                    value.add(codec)
                } else {
                    throw IllegalArgumentException("Mimetype $mimeType is not supported by enhanced RTMP protocol")
                }
            }
            return ExVideoCodecs(value.joinToString(","))
        }
    }
}