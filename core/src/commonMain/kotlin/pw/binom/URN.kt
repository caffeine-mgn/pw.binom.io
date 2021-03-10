package pw.binom

/**
 * Contains URN String
 * @see <a href="https://en.wikipedia.org/wiki/Uniform_Resource_Name">URN</a>
 */
inline class URN internal constructor(val raw: String) {
    /**
     * Returns true if this urn match for [mask]. Path Variables is support.
     * Example: `"/users/100500/info_data".toURN.isMatch("/users/{id}/info_*")` returns true
     */
    fun isMatch(mask: String) = raw.isPathMatch(mask, 0, 0)

    /**
     * Returns variable from this urn. Will find [name] inside this, using [mask].
     * Example: `"/users/100500/info_data".toURN` with call getVariable("id", "/users/{id}/info_*") will return `100500`
     * @param mask mask for getting veriable
     */
    fun getVariable(name: String, mask: String): String? {
        var str: String? = null
        raw.findPathVariable(mask, name, 0, 0) {
            str = it
        }
        return str
    }

    fun appendDirection(direction: String, separator: String = "/") =
        "${raw.removeSuffix(separator)}$separator${direction.removePrefix(separator)}".toURN
}

val String.toURN
    get() = URN(this)