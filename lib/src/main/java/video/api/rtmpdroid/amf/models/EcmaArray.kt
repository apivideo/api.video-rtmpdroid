package video.api.rtmpdroid.amf.models

/**
 * A ECMA array
 */
class EcmaArray {
    /**
     * List of added parameters
     */
    internal val parameters = mutableListOf<Any>()

    /**
     * Adds a new parameter inside the ECMA array
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
}