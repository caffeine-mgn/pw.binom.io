package pw.binom.io.file

interface RandomAccess {
    var position: ULong
    val size: ULong
}