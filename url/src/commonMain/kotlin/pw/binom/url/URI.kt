package pw.binom.url

import kotlin.jvm.JvmInline

@JvmInline
value class URI(private val raw: String) {

    companion object {
        fun new(schema: String?, path: Path?, query: Query?, fragment: String?): URI {
            val sb = StringBuilder()
            if (schema != null) {
                if (schema.isEmpty()) {
                    sb.append("//")
                } else {
                    sb.append(schema).append("://")
                }
            }
            if (path != null) {
                sb.append(path.toString())
            }
            if (query != null) {
                sb.append("?").append(query.toString())
            }
            if (fragment != null) {
                sb.append("#").append(fragment.toString())
            }
            return URI(sb.toString())
        }
    }

    fun copy(
        schema: String? = this.schema,
        path: Path? = this.path,
        query: Query? = this.query,
        fragment: String? = this.fragment
    ) = new(schema = schema, path = path, query = query, fragment = fragment)

    val schema: String?
        get() {
            val p = raw.indexOf("//")
            if (p == -1) {
                return null
            }
            if (p == 0) {
                return ""
            }
            return raw.substring(0, p - 1)
        }
    val query: Query?
        get() {
            val s = raw.indexOf('?')
            if (s == -1) {
                return null
            }
            var e = raw.indexOf('#')
            if (e == -1) {
                e = raw.length
            }
            return raw.substring(s + 1, e).toQuery
        }

    val fragment: String?
        get() {
            val s = raw.indexOf('#')
            if (s == -1) {
                return null
            }
            return raw.substring(s)
        }

    val path: Path
        get() {
            var start = 0
            var end = raw.length
            val schemaIndex = raw.indexOf("//")
            if (schemaIndex != -1) {
                start = schemaIndex + 2
            }
            val queryIndex = raw.indexOf('?', startIndex = start)
            if (queryIndex != -1) {
                end = queryIndex
            } else {
                val fragmentIndex = raw.indexOf('#', startIndex = start)
                if (fragmentIndex != -1) {
                    end = fragmentIndex
                }
            }
            if (start == end) {
                return Path.EMPTY
            }
            return Path(raw.substring(startIndex = start, endIndex = end))
        }

    override fun toString(): String = raw
    val hasSchema: Boolean
        get() {
            val s = schema
            return s.isNullOrEmpty()
        }

    fun appendQuery(key: String, value: String? = null): URI =
        copy(query = query?.append(key = key, value = value) ?: Query.new(key = key, value = value))

  fun appendQuery(values:Map<String,String?>): URI =
    copy(query = query?.append(values) ?: Query.new(values))

    fun appendPath(path: Path) = copy(
        path = this.path.append(path)
    )

    fun toURL(): URL {
        val schema = schema
        if (schema.isNullOrEmpty()) {
            throw MalformedURLException("Can't create URL from URI. Schema missing. Uri: \"$raw\"")
        }
        return raw.toURL()
    }

    operator fun plus(toPath: Path) = copy(path = path.append(toPath))

    fun isMatch(mask: String) = pathMatch(raw, mask)
    fun isMatch(mask: PathMask) = isMatch(mask.raw)
}
