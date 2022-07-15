package pw.binom.net

import pw.binom.pathMatch
import kotlin.jvm.JvmInline

/**
 * Contains Path String
 */
@JvmInline
value class Path internal constructor(val raw: String) {
    companion object {
        val EMPTY = Path("")
    }

    /**
     * Returns true if this urn match for [mask]. Path Variables is support.
     * Example: `"/users/100500/info_data".toURN.isMatch("/users/{id}/info_*")` returns true
     */
    fun isMatch(mask: String) = pathMatch(raw, mask)
    fun isMatch(mask: PathMask) = isMatch(mask.raw)

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

    fun getVariables(mask: PathMask, dest: MutableMap<String, String> = HashMap()) =
        getVariables(
            mask = mask.raw,
            dest = dest,
        )

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

    val normalize: Path
        get() {
            val el = elements
            if (el.any { it == ".." || it == "." }) {
                return this
            }
            val elements = ArrayList<String>(el.size)
            el.forEach {
                when (it) {
                    "." -> return@forEach
                    ".." -> if (elements.removeLastOrNull() == null) {
                        throw IllegalArgumentException("Can't normalize path \"$raw\" is invalid. Address is invalid")
                    }
                    else -> elements += it
                }
            }
            return Path(elements.joinToString("/"))
        }

    val root: String
        get() {
            val p = raw.indexOf('/')
            if (p == -1) {
                return raw
            }
            return raw.substring(0, p)
        }

    fun removeRoot(): Path? {
        val p = raw.indexOf('/')
        if (p == -1) {
            return null
        }
        return Path(raw.substring(p + 1))
    }

    fun relative(path: Path) = relative(path.raw)

    /**
     * Append [path] to current path with relative rules. You can use `.` and `..` for [path]
     * For example `"/home/subochev".toPath.relative(".ssh")` returns `"/home/subochev/.ssh"`.
     * And if [path] starts with `/` [path] will override full path. For example
     * `"/home/subochev".toPath.relative("/home/admin")` returns `"/home/admin"`
     */
    fun relative(path: String): Path {
        if (path.startsWith("/")) {
            return path.toPath
        }
        val currentPath = this.raw.split('/').toMutableList()
        val newPath = path.split('/')
        newPath.forEach {
            when (it) {
                "." -> {
                }
                ".." -> {
                    if (currentPath.isEmpty()) {
                        throw IllegalArgumentException("Can't find relative from \"${this.raw}\" to \"$path\"")
                    }
                    currentPath.removeLast()
                }
                else -> currentPath.add(it)
            }
        }
        return Path(currentPath.joinToString("/"))
    }

    val name: String
        get() {
            val p = raw.lastIndexOf('/')
            return if (p == -1) {
                raw
            } else {
                raw.substring(p + 1)
            }
        }

    val parent: Path?
        get() {
            val p = raw.lastIndexOf('/')
            return if (p == -1) {
                null
            } else {
                raw.substring(0, p).toPath
            }
        }

    override fun toString(): String = raw
    operator fun plus(path: Path) = Path("$raw/${path.raw}")

    val toURI
        get() = URI(raw)

//    fun toUri() = UTF8.urlEncode(raw).toURL()
//    fun toUri(root: URL): URL {
//        val v = toUri
//        if (v != null) {
//            return v
//        }
//        if (raw.startsWith("/")) {
//            return root.copy(path = this)
//        }
//        val p = root.path.parent
//        val newPath = if (p == null) {
//            "/$raw".toPath
//        } else {
//            p.relative(raw)
//        }
//        return root.copy(
//            path = newPath,
//        )
//    }
}

val String.toPath
    get() = Path(this)
