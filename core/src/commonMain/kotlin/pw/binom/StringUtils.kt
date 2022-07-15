@file:JvmName("CommonStringUtilsKt")

package pw.binom

import kotlin.jvm.JvmName

class InvalidPathException(val path: String) : RuntimeException() {
    override val message: String?
        get() = "Invalid path \"$path\""
}

fun String.isWildcardMatch(wildcard: String): Boolean = wildcardMatch(this, wildcard)
fun String.parsePathMask(
    variable: ((variable: String, position: Int) -> Unit)? = null,
    wildcard: ((variable: String, position: Int) -> Unit)? = null,
    text: ((variable: String, position: Int) -> Unit)? = null,
) {
    internalParsePathMask(
        variable = variable,
        wildcard = wildcard,
        text = text,
        mask = this,
    )
}

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
                if (wildBackup == -1) {
                    return false
                }
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

internal fun internalParsePathMask(
    mask: String,
    variable: ((variable: String, position: Int) -> Unit)? = null,
    wildcard: ((variable: String, position: Int) -> Unit)? = null,
    text: ((variable: String, position: Int) -> Unit)? = null,
) {
    var cursor = 0

    fun readVariable() {
        cursor++
        val cursorStart = cursor
        while (cursor < mask.length) {
            when (mask[cursor]) {
                '}' -> {
                    if (variable != null) {
                        variable(mask.substring(cursorStart, cursor), cursorStart)
                    }
                    cursor++
                    return
                }
                else -> cursor++
            }
        }
        throw InvalidPathException("Invalid mask \"$mask\": variable \"${mask.substring(cursorStart)}\" not finished")
    }

    fun readWildcard() {
        if (wildcard != null) {
            wildcard(mask[cursor].toString(), cursor)
        }
        cursor++
    }

    var cursorStart = 0

    fun callText() {
        if (cursor > cursorStart && text != null) {
            text(mask.substring(cursorStart, cursor), cursorStart)
        }
    }

    while (cursor < mask.length) {
        when (mask[cursor]) {
            '{' -> {
                callText()
                readVariable()
                cursorStart = cursor
            }
            '*', '?' -> {
                callText()
                readWildcard()
                cursorStart = cursor
            }
            else -> cursor++
        }
    }
    callText()
}

internal fun pathMatch(
    path: String,
    mask: String,
    func: (key: String, value: String) -> Unit = { _, _ -> },
): Boolean {
    var text = 0
    var wild = 0
    var textBackup = -1
    var wildBackup = -1
    var textVariableStart = -1
    var textVariableStartBackup = -1
    var wildVariableStart = -1
//    var startWithSkipPath = false
    while (path.length != text) {
        when {
//            mask(wild) == '*' && mask(wild + 1) == '*' -> {
//                // invalid path pattern
//                if (textVariableStart != -1) {
//                    throw InvalidPathException(mask)
//                }
//                // new star-loop: backup positions in pattern and text
//                wild += 2
//                textBackup = text
//                wildBackup = wild
//                startWithSkipPath = true
//            }
            mask(wild) == '*' -> {
                // invalid path pattern
                if (textVariableStart != -1) {
                    throw InvalidPathException(mask)
                }
                // new star-loop: backup positions in pattern and text
                textBackup = text
                wildBackup = ++wild
//                startWithSkipPath = false
            }
            mask(wild) == '{' -> {
                val index = mask.indexOf('}', wild)
                val p = mask.indexOf('{', wild + 1)
                // invalid path pattern
                if (index == -1 || (p != -1 && p < index)) {
                    throw InvalidPathException(mask)
                }
                wildVariableStart = wild + 1
                textBackup = text
                wild = index + 1
                wildBackup = wild
                textVariableStart = text
                textVariableStartBackup = text
            }
            mask(wild) == path(text) && textVariableStart != -1 -> {
                val value = path.substring(textVariableStart, text)
                val key = mask.substring(wildVariableStart, wild - 1)
                func(key, value)
                textVariableStart = -1
                text++
                wild++
                wildBackup = wild + key.length
            }
            mask(wild) == '?' || mask(wild) == path(text) -> {
                // ? matched any character or we matched the current non-NUL character
                text++
                wild++
            }
            textVariableStart != -1 && (path(text) == '/' || path(text) == '\\') -> {
                return false
            }
            else -> {
                // if no stars we fail to match
                if (wildBackup == -1) {
                    return false
                }
                // star-loop: backtrack to the last * by restoring the backup positions
                // in the pattern and text
                if (textVariableStartBackup != -1) {
                    textVariableStart = textVariableStartBackup
                }
                text = ++textBackup
                wild = wildBackup
            }
        }
    }
    // ignore trailing stars
    while (mask(wild) == '*') {
        wild++
    }
    if (mask.length == wild && textVariableStart != -1) {
        val value = path.substring(textVariableStart, text)
        val key = mask.substring(wildVariableStart, wild - 1)
        func(key, value)
        return true
    }
    // at end of text means success if nothing else is left to match
    return mask.length == wild
}
