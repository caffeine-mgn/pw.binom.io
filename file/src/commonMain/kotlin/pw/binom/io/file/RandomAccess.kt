package pw.binom.io.file

interface RandomAccess {
    var position: Long
    val size: Long
}
