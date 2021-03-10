@file:JvmName("CommonStringUtilsKt")
package pw.binom

import pw.binom.charset.Charset
import pw.binom.charset.Charsets
import kotlin.jvm.JvmName

expect fun ByteArray.decodeString(
    charset: Charset = Charsets.UTF8,
    offset: Int = 0,
    length: Int = size - offset
): String

expect fun String.encodeBytes(charset: Charset = Charsets.UTF8): ByteArray

fun String.isWildcardMatch(wildcard: String): Boolean = isWildcardMatch(wildcard, w = 0, s = 0)

private fun String.isWildcardMatch(wildcard: String, s: Int, w: Int): Boolean {
    if (w == wildcard.length && s == length)
        return true
    if (w == wildcard.length)
        return false
    if (wildcard[w] == '*' && wildcard.length - w > 1 && length == s)
        return false
    if (wildcard[w] == '?' || (length != s && wildcard[w] == this[s]))
        return isWildcardMatch(wildcard, s = s + 1, w = w + 1)
    if (wildcard[w] == '*')
        return isWildcardMatch(wildcard, s = s, w = w + 1) || isWildcardMatch(wildcard, w = w, s = s + 1)
    return false
}

internal fun String.isPathMatch(wildcard: String, s: Int, w: Int): Boolean {
    if (w == wildcard.length && s == length) {
        return true
    }
    if (w == wildcard.length) {
        return false
    }
    if (wildcard[w] == '*' && wildcard.length - w > 1 && length == s) {
        return false
    }
    if (wildcard[w] == '?' || (length != s && wildcard[w] == this[s])) {
        return isPathMatch(wildcard, s = s + 1, w = w + 1)
    }
    if (wildcard[w] == '*') {
        return isPathMatch(wildcard, s = s, w = w + 1) || isPathMatch(wildcard, w = w, s = s + 1)
    }
    if (wildcard[w] == '{') {
        val we = wildcard.indexOf('}', w)
        if (we == -1) {
            return false
        }
        return isPathMatch(wildcard, s = s, w = we + 1) || isPathMatch(wildcard, w = we, s = s + 1)
    }
    if (wildcard[w] == '}') {
        return isPathMatch(wildcard, s = s, w = w + 1) || isPathMatch(wildcard, w = w, s = s + 1)
    }
    return false
}

internal fun String.findPathVariable(wildcard: String, path: String, s: Int, w: Int, func: (String) -> Unit): Int {
    operator fun String.invoke(index: Int) = if (index >= length) '\u0000' else this[index]
    if (w == wildcard.length && s == length) {
        return s
    }
    if (w == wildcard.length) {
        return -1
    }
    if (wildcard[w] == '}' && /*w + 1 < wildcard.length && s != length &&*/ this(s) == wildcard(w + 1)) {
        return s
    }
    if (s == length) {
        return -1
    }
    if (wildcard[w] == '*' && wildcard.length - w > 1 && length == s) {
        return -1
    }
    if (wildcard[w] == '?' || (length != s && wildcard[w] == this[s])) {
        return findPathVariable(wildcard, s = s + 1, w = w + 1, path = path, func = func)
    }
    if (wildcard[w] == '*') {
        val p1 = findPathVariable(wildcard, s = s, w = w + 1, path = path, func = func)
        if (p1 != -1) {
            return p1
        }
        val p2 = findPathVariable(
            wildcard,
            w = w,
            s = s + 1,
            path = path,
            func = func,
        )
        if (p2 != -1) {
            return p2
        }
        return -1
    }
    if (wildcard[w] == '{') {
        val we = wildcard.indexOf('}', w)
        if (we == -1) {
            return -1
        }
        val found = wildcard.substring(w + 1, we) == path
        val p1 = findPathVariable(wildcard, s = s, w = we + 1, path = path, func = func)
        if (p1 != -1) {
            if (found) {
                func(substring(s, p1))
                return -1
            }
            return p1
        }
        val p2 = findPathVariable(wildcard, w = we, s = s + 1, path = path, func = func)
        if (p2 != -1) {
            if (found) {
                func(substring(s, p2))
                return -1
            }
            return p2
        }
        return -1
    }
    if (wildcard[w] == '}') {
        val p1 = findPathVariable(wildcard, s = s, w = w + 1, path = path, func = func)
        if (p1 != -1) {
            return p1
        }
        val p2 = findPathVariable(wildcard, w = w, s = s + 1, path = path, func = func)
        if (p2 != -1) {
            return p2
        }
        return -1
    }
    return -1
}