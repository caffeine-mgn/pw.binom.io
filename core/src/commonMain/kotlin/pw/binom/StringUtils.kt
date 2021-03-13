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
class InvalidPathException(val path: String) : RuntimeException() {
    override val message: String?
        get() = "Invalid path \"$path\""
}

fun String.isWildcardMatch(wildcard: String): Boolean = wildcardMatch(this, wildcard)

private inline operator fun String.invoke(index: Int) = if (index >= length) '\u0000' else this[index]

// returns TRUE if text string matches glob-like pattern with * and ?
internal fun wildcardMatch(string: String, wildcard: String): Boolean {
    var text = 0
    var wild = 0

    var textBackup = -1
    var wildBackup = -1
    while (string.length != text) {
        when {
            wildcard(wild) == '*' -> {
                // new star-loop: backup positions in pattern and text
                textBackup = text
                wildBackup = ++wild
            }
            wildcard(wild) == '?' || wildcard(wild) == string(text) -> {
                // ? matched any character or we matched the current non-NUL character
                text++
                wild++
            }
            else -> {
                // if no stars we fail to match
                if (wildBackup == -1)
                    return false
                // star-loop: backtrack to the last * by restoring the backup positions
                // in the pattern and text
                text = ++textBackup
                wild = wildBackup
            }
        }
    }
    // ignore trailing stars
    while (wildcard(wild) == '*') {
        wild++
    }
    // at end of text means success if nothing else is left to match
    return wildcard.length == wild
}

internal fun pathMatch(
    path: String,
    mask: String,
    func: (key: String, value: String) -> Boolean = { _, _ -> true }
): Boolean {
    var text = 0
    var wild = 0

    var textBackup = -1
    var wildBackup = -1
    var tVariableStart = -1
    var wVariableStart = -1
    while (path.length != text) {
        when {
            mask(wild) == '*' -> {
                //invalid path pattern
                if (tVariableStart != -1) {
                    throw InvalidPathException(mask)
                }
                // new star-loop: backup positions in pattern and text
                textBackup = text
                wildBackup = ++wild
            }
            mask(wild) == '{' -> {
                val index = mask.indexOf('}', wild)
                //invalid path pattern
                if (index == -1) {
                    throw InvalidPathException(mask)
                }
                wVariableStart = wild + 1
                textBackup = text
                wild = index + 1
                wildBackup = wild
                tVariableStart = text
            }
            mask(wild) == path(text) && tVariableStart != -1->{
                val value = path.substring(tVariableStart, text)
                val key = mask.substring(wVariableStart, wild - 1)
                if (!func(key, value)) {
                    return true
                }
                tVariableStart = -1
                wildBackup = wild
                textBackup = text
                text++
                wild++
            }
            mask(wild) == '?' || mask(wild) == path(text) -> {
                // ? matched any character or we matched the current non-NUL character
                text++
                wild++
            }
            else -> {
                // if no stars we fail to match
                if (wildBackup == -1)
                    return false
                // star-loop: backtrack to the last * by restoring the backup positions
                // in the pattern and text
                text = ++textBackup
                wild = wildBackup
            }
        }
    }
    // ignore trailing stars
    while (mask(wild) == '*') {
        wild++
    }
    if (mask.length == wild && tVariableStart != -1) {
        val value = path.substring(tVariableStart, text)
        val key = mask.substring(wVariableStart, wild - 1)
        func(key, value)
        return true
    }
    // at end of text means success if nothing else is left to match
    return mask.length == wild
}