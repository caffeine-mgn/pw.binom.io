package pw.binom.net

import pw.binom.pathMatch
import kotlin.jvm.JvmInline

/**
 * Contains Path String
 */
@JvmInline
value class Path internal constructor(val raw: String) {
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

    /**
     * Parse current path with [mask]. In [dest] will put all variable values.
     * You can define your variable using `{` and `}`. Like this `/users/{id}/info`.
     * Defined variable work like `*`, but variable will write into [dest]
     * Example:
     *
     * Path: `/users/test/admin/info`, Mask: `/users/{id}/info`
     *
     * [dest] will contents one pair: "id", "test/admin"
     *
     * @param mask mask for match. Example: `/users/{id}/info`
     * @param dest map for put parsed variables. Default value is `new HashMap<String,String>()`
     * @return Returns [dest] if path is match. If path is not match will returns null
     */
    fun getVariables(mask: String, dest: MutableMap<String, String> = HashMap()): MutableMap<String, String>? {
        if (!pathMatch(raw, mask) { key, value -> dest[key] = value }) {
            return null
        }
        return dest
    }

    /**
     * Call [func] on each variable substring. You should get last value of key.
     * can call `func("path","")`, then `func("path","/user")` and then `func("path","/user/list")`.
     * In this case you should get last value: `/user/list`.
     *
     * Also [func] can be call even path have invalid [mask]. You should get result only of
     * [eachVariables] returns true
     */
    fun eachVariables(mask: String, func: (key: String, value: String) -> Unit) =
        pathMatch(raw, mask, func = func)

    fun append(path: String, direction: Boolean = true) =
        if (direction) {
            "${raw.removeSuffix("/")}/${path.removePrefix("/")}".toPath
        } else {
            "$raw$path".toPath
        }

    fun append(path: Path): Path = append(path.raw, true)

    val elements
        get() = raw.split('/')

    val name: String
        get() {
            val p = raw.lastIndexOf('/')
            return if (p == -1)
                raw
            else
                raw.substring(p + 1)
        }

    val parent: Path?
        get() {
            val p = raw.lastIndexOf('/')
            return if (p == -1)
                null
            else
                raw.substring(0, p).toPath
        }

    override fun toString(): String = raw
}

val String.toPath
    get() = Path(this)