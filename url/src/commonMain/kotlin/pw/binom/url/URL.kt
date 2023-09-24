package pw.binom.url

import kotlin.jvm.JvmInline

@JvmInline
value class URL internal constructor(val fullPath: String) {
  companion object {
    fun new(
      schema: String,
      user: String?,
      password: String?,
      host: String,
      port: Int?,
      path: Path,
      query: Query?,
      fragment: String?,
    ): String {
      require(schema.isNotEmpty()) { "Schema is empty" }
      val sb = StringBuilder()
      sb.append(schema).append(":").append("//")
      if (user != null) {
        sb.append(user)
      }

      if (password != null) {
        sb.append(":")
        sb.append(password)
      }
      if (user != null) {
        sb.append("@")
      }
      sb.append(host)
      if (port != null) {
        sb.append(":").append(port)
      }
      sb.append(UrlEncoder.pathEncode(path.raw))
      if (query != null && query.isNotEmpty) {
        sb.append("?").append(query)
      }
      if (fragment != null) {
        sb.append("#").append(UrlEncoder.encode(fragment))
      }
      return sb.toString()
    }
  }

  val schema: String
    get() {
      val p = fullPath.indexOf("//")
      if (p == 0) {
        throw MalformedURLException(fullPath)
      }
      return fullPath.substring(0, p - 1)
    }
  val user: String?
    get() {
      val p1 = fullPath.indexOf('@')
      if (p1 == -1) {
        return null
      }
      val p = fullPath.indexOf("//") + 2
      val b = fullPath.indexOf(':', p + 2)
      if (b == -1 || b > p1) {
        return fullPath.substring(p, p1)
      }

      return fullPath.substring(p, b)
    }
  val password: String?
    get() {
      val p1 = fullPath.indexOf('@')
      if (p1 == -1) {
        return null
      }
      val p = fullPath.indexOf("//") + 2
      val b = fullPath.indexOf(':', p + 2)
      if (b == -1 || b > p1) {
        return null
      }

      return fullPath.substring(b + 1, p1)
    }
  val host: String
    get() {
      var p = fullPath.indexOf('@') + 1
      if (p == 0) {
        p = fullPath.indexOf("//") + 2
      }

      var e = fullPath.indexOf(':', p)
      if (e == -1) {
        e = fullPath.indexOf('/', p + 1)
      }
      if (e == -1) {
        e = fullPath.indexOf('?', p + 1)
      }

      if (e == -1) {
        e = fullPath.indexOf('#', p + 1)
      }
      if (e == -1) {
        e = fullPath.length
      }
      return fullPath.substring(p, e)
    }
  val port: Int?
    get() {
      var s = fullPath.indexOf("//") + 2
      s = fullPath.indexOf(":", s)
      val c = fullPath.indexOf('@')
      if (c != -1) {
        s = fullPath.indexOf(":", c + 1)
        if (s == -1) {
          return null
        }
      }
      if (s == -1) {
        return null
      }
      s++

      val hash = fullPath.indexOf('#', s)
      val query = fullPath.indexOf('?', s)
      val end = fullPath.indexOf('/', s)
      var e = fullPath.length
      e = when {
        end != -1 && end < e -> end
        query != -1 && query < e -> query
        hash != -1 && hash < e -> hash
        else -> e
      }
      return fullPath.substring(s, e).toInt()
    }
  val path: Path
    get() {
      var s = fullPath.indexOf("//") + 2
      s = fullPath.indexOf('/', s)
      if (s == -1) {
        return "".toPath
      }
      var e = fullPath.indexOf('?')
      if (e == -1) {
        e = fullPath.indexOf('#')
      }
      if (e == -1) {
        e = fullPath.length
      }
      return fullPath.substring(s, e).split('/').map { UrlEncoder.decode(it) }.joinToString("/").toPath
    }

  /**
   *
   * Returns http request [path]+[query]
   */
  val request: String
    get() {
      val q = query
      return if (q == null) {
        path.raw
      } else {
        "${path.raw}?$q"
      }
    }
  val query: Query?
    get() {
      val s = fullPath.indexOf('?')
      if (s == -1) {
        return null
      }
      var e = fullPath.indexOf('#')
      if (e == -1) {
        e = fullPath.length
      }
      return fullPath.substring(s + 1, e).toQuery
    }
  val fragment: String?
    get() {
      val s = fullPath.indexOf('#')
      if (s == -1) {
        return null
      }
      return UrlEncoder.decode(fullPath.substring(s))
    }

  fun copy(
    schema: String = this.schema,
    user: String? = this.user,
    password: String? = this.password,
    host: String = this.host,
    port: Int? = this.port,
    path: Path = this.path,
    query: Query? = this.query,
    fragment: String? = this.fragment,
  ) = URL(
    new(
      schema = schema,
      user = user,
      password = password,
      host = host,
      port = port,
      path = path,
      query = query,
      fragment = fragment
    )
  )

  /**
   * Returns full encoded uri
   */
  override fun toString() = fullPath

  /**
   * Append [path] to current uri path and returns new URI
   * @param path Path for append
   * @param direction flag for add "/" between old value of path and appending [path]
   * @param encode flag for automatic encode appending [path]
   * @return URI with appended [path]
   */
  fun appendPath(path: String, direction: Boolean = true, encode: Boolean = false) = run {
    val result = this.path.append(path = if (encode) UrlEncoder.pathEncode(path) else path, direction = direction)
    copy(
      path = result
    )
  }

  fun addPath(path: Path) = appendPath(path = path.toString(), direction = true, encode = true)

  fun appendQuery(key: String) =
    copy(query = query?.append(key = key, value = null) ?: Query.new(key = key, value = null))

  fun appendQuery(key: String, value: String) =
    copy(query = query?.append(key = key, value = value) ?: Query.new(key = key, value = value))

  fun appendQuery(key: String, value: Int) = appendQuery(key = key, value = value.toString())

  fun appendQuery(key: String, value: Long) = appendQuery(key = key, value = value.toString())

  fun appendQuery(key: String, value: Float) = appendQuery(key = key, value = value.toString())

  fun appendQuery(key: String, value: Byte) = appendQuery(key = key, value = value.toString())

  fun appendQuery(key: String, value: Boolean) = appendQuery(key = key, value = value.toString())

  fun appendQuery(values: Map<String, String?>) = copy(query = query?.append(values) ?: Query.new(values))

  fun toURI() = URI(fullPath)

  fun goto(other: URI): URL {
    val otherSchema = other.schema
    val schema = if (otherSchema.isNullOrEmpty()) this.schema else otherSchema
    if (otherSchema != null && otherSchema.isNotEmpty()) {
      return other.toURL()
    }
    val newPath = (path.parent ?: Path.EMPTY).relative(other.path)
    return copy(
      schema = schema,
      path = newPath,
      query = other.query ?: query,
      fragment = other.fragment ?: fragment,
    )
  }

  operator fun plus(toPath: Path) = copy(path = path.append(toPath))

  fun isMatch(mask: String) = pathMatch(fullPath, mask)
  fun isMatch(mask: PathMask) = isMatch(mask.raw)
}
