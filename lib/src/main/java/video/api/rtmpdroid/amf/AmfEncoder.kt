package video.api.rtmpdroid.amf

import video.api.rtmpdroid.RtmpNativeLoader
import video.api.rtmpdroid.amf.models.EcmaArray
import video.api.rtmpdroid.amf.models.NamedParameter
import video.api.rtmpdroid.amf.models.NullParameter
import video.api.rtmpdroid.amf.models.ObjectParameter
import java.io.IOException
import java.nio.ByteBuffer

class AmfEncoder {
    private val parameters = mutableListOf<Any>()

    /**
     * Adds a new parameter.
     * Once your are done adding parameters, call [encode].
     *
     * @param parameter the new parameter
     */
    fun add(parameter: Any) {
        parameters.add(parameter)
    }

    /**
     * Adds a named parameter.
     * Same as adding a NamedParameter.
     *
     * @param name the parameter name
     * @param value the parameter value
     */
    fun add(name: String, value: Any) {
        parameters.add(NamedParameter(name, value))
    }

    /**
     * Encodes added parameters.
     *
     * @return a [ByteBuffer] containing added parameters in AMF
     */
    fun encode(): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(minBufferSize)
        encode(buffer)
        buffer.rewind()
        return buffer
    }

    /**
     * Encodes added parameters.
     *
     * @param buffer a direct buffer
     */
    fun encode(buffer: ByteBuffer) {
        require(buffer.isDirect) { "ByteBuffer must be a direct buffer" }

        parameters.forEach {
            encode(buffer, it)
        }
    }

    private fun encode(buffer: ByteBuffer, parameter: Any) {
        val size = when (parameter) {
            is Boolean -> {
                nativeEncodeBoolean(buffer, parameter = parameter)
            }
            is Int -> {
                nativeEncodeInt(buffer, parameter = parameter)
            }
            is Double -> {
                nativeEncodeNumber(buffer, parameter = parameter)
            }
            is String -> {
                nativeEncodeString(buffer, parameter = parameter)
            }
            is NullParameter -> {
                buffer.put(AmfType.NULL.value)
                buffer.position()
            }
            is NamedParameter -> {
                when (parameter.value) {
                    is Boolean -> {
                        nativeEncodeNamedBoolean(
                            buffer,
                            name = parameter.name,
                            parameter = parameter.value
                        )
                    }
                    is Double -> {
                        nativeEncodeNamedNumber(
                            buffer,
                            name = parameter.name,
                            parameter = parameter.value
                        )
                    }
                    is String -> {
                        nativeEncodeNamedString(
                            buffer,
                            name = parameter.name,
                            parameter = parameter.value
                        )
                    }
                    else -> {
                        throw IOException("Named parameter type is not supported: ${parameter.value::class.java.simpleName}")
                    }
                }
            }
            is ObjectParameter -> {
                buffer.put(AmfType.OBJECT.value)
                parameter.parameters.forEach { encode(buffer, it) }
                nativeEncodeInt24(buffer, parameter = AmfType.OBJECT_END.value.toInt())
            }
            is EcmaArray -> {
                buffer.put(AmfType.ECMA_ARRAY.value)
                buffer.position(nativeEncodeInt(buffer, parameter = parameter.parameters.size))
                parameter.parameters.forEach { encode(buffer, it) }
                nativeEncodeInt24(buffer, parameter = AmfType.OBJECT_END.value.toInt())
            }
            else -> throw IOException("Parameter type is not supported: ${parameter::class.java.simpleName}")
        }
        if (size < 0) {
            throw ArrayIndexOutOfBoundsException(buffer.position())
        }
        buffer.position(size)
    }

    /**
     * Get buffer size in bytes.
     */
    val minBufferSize: Int
        get() = parameters.sumOf { getParameterSize(it) }

    companion object {
        init {
            RtmpNativeLoader
        }

        private fun getParameterSize(parameter: Any): Int {
            return when (parameter) {
                is Boolean -> {
                    2
                }
                is Short -> {
                    2
                }
                is Int -> {
                    4
                }
                is Double -> {
                    9
                }
                is String -> {
                    3 + parameter.length
                }
                is NullParameter -> {
                    1
                }
                is NamedParameter -> {
                    2 /* includes param name size (2 bytes) */ + parameter.name.length + getParameterSize(
                        parameter.value
                    )
                }
                is ObjectParameter -> {
                    4 /* 1 byte for start - 3 bytes for footer */ + parameter.parameters.sumOf {
                        getParameterSize(
                            it
                        )
                    }
                }
                is EcmaArray -> {
                    8 /* 1 byte for type + 4 bytes for array size + 3 bytes for footer */ + parameter.parameters.sumOf {
                        getParameterSize(
                            it
                        )
                    }
                }
                else -> throw IOException("Parameter type is not supported: ${parameter::class.java.simpleName}")
            }
        }

        @JvmStatic
        private external fun nativeEncodeBoolean(
            buffer: ByteBuffer,
            offset: Int = buffer.position(),
            end: Int = buffer.limit(),
            parameter: Boolean
        ): Int

        @JvmStatic
        private external fun nativeEncodeInt24(
            buffer: ByteBuffer,
            offset: Int = buffer.position(),
            end: Int = buffer.limit(),
            parameter: Int
        ): Int

        @JvmStatic
        private external fun nativeEncodeInt(
            buffer: ByteBuffer,
            offset: Int = buffer.position(),
            end: Int = buffer.limit(),
            parameter: Int
        ): Int

        @JvmStatic
        private external fun nativeEncodeNumber(
            buffer: ByteBuffer,
            offset: Int = buffer.position(),
            end: Int = buffer.limit(),
            parameter: Double
        ): Int

        @JvmStatic
        private external fun nativeEncodeString(
            buffer: ByteBuffer,
            offset: Int = buffer.position(),
            end: Int = buffer.limit(),
            parameter: String
        ): Int

        @JvmStatic
        private external fun nativeEncodeNamedBoolean(
            buffer: ByteBuffer,
            offset: Int = buffer.position(),
            end: Int = buffer.limit(),
            name: String,
            parameter: Boolean
        ): Int

        @JvmStatic
        private external fun nativeEncodeNamedNumber(
            buffer: ByteBuffer,
            offset: Int = buffer.position(),
            end: Int = buffer.limit(),
            name: String,
            parameter: Double
        ): Int

        @JvmStatic
        private external fun nativeEncodeNamedString(
            buffer: ByteBuffer,
            offset: Int = buffer.position(),
            end: Int = buffer.limit(),
            name: String,
            parameter: String
        ): Int
    }
}

enum class AmfType(val value: Byte) {
    NUMBER(0x00),
    BOOLEAN(0x01),
    STRING(0x02),
    OBJECT(0x03),
    NULL(0x05),
    ECMA_ARRAY(0x08),
    OBJECT_END(0x09)
}