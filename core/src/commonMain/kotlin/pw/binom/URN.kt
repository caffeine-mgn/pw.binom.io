package pw.binom

inline class URN internal constructor(val raw: String) {
    fun isMatch(mask: String) = raw.isPathMatch(mask, 0, 0)

    fun getVariable(pathName: String, mask: String): String? {
        var str: String? = null
        raw.findPathVariable(mask, pathName, 0, 0) {
            str = it
        }
        return str
    }
}

val String.toURN
    get() = URN(this)