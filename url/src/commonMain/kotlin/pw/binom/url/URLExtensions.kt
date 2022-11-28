package pw.binom.url

/**
 * Convert current string to [URL]. If current string is invalid URI will throw [MalformedURLException]
 * Input string should be url encoded
 * @throws MalformedURLException If current string is invalid URI
 */
fun String.toURL() = toURLOrNull ?: throw MalformedURLException(this)

/**
 * Convert current string to [URL]. If current string is invalid URI will return null
 * Input string should be url encoded
 */
val String.toURLOrNull
    get(): URL? {
        val p = indexOf("//")
        if (p == -1) {
            return null
        }
        if (indexOf("//", p + 1) != -1) {
            return null
        }
        val q = indexOf("?")
        if (q != -1 && q < p) {
            return null
        }
        if (indexOf("?", q + 1) != -1) {
            return null
        }

        val h = indexOf("#")
        if (h != -1 && q > 0 && h < q) {
            return null
        }
        if (indexOf("#", h + 1) != -1) {
            return null
        }

        return URL(this)
    }
