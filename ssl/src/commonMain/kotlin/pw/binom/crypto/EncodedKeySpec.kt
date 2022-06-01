package pw.binom.crypto

sealed interface EncodedKeySpec : KeySpec {
    val data: ByteArray
    val format: String
}
