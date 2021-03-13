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
    fun isMatch(mask: String) = pathMatch(raw, mask)

    /**
     * Returns variable from this urn. Will find [name] inside this, using [mask].
     * Example: `"/users/100500/info_data".toURN` with call getVariable("id", "/users/{id}/info_*") will return `100500`
     * @param mask mask for getting veriable
     */
    fun getVariable(name: String, mask: String): String? {
        var str: String? = null
        eachVariables(mask = mask) { key, value ->
            if (key == name) {
                str = value
                false
            } else {
                true
            }
        }
        return str
    }

    fun getVariables(mask: String): Map<String, String> {
        val out = HashMap<String, String>()
        getVariables(mask = mask, desc = out, ignoreDuplicate = false)
        return out
    }

    fun getVariables(mask: String, desc: MutableMap<String, String>, ignoreDuplicate: Boolean = true) {
        pathMatch(raw, mask) { key, value ->
            println("$key=$value")
            if (!ignoreDuplicate && desc.containsKey(key)) {
                throw IllegalArgumentException("Duplicate Path Variable \"$key\". urn: \"$raw\", mask: \"$mask\"")
            }
            desc[key] = value
            true
        }
    }

    fun eachVariables(mask: String, func: (key: String, value: String) -> Boolean) =
        pathMatch(raw, mask, func = func)

    fun appendDirection(direction: String, separator: String = "/") =
        "${raw.removeSuffix(separator)}$separator${direction.removePrefix(separator)}".toURN

    override fun toString(): String = raw
}

val String.toURN
    get() = URN(this)