package pw.binom.crypto

actual enum class RsaPadding(val jvmName: String) {
    PKCS1Padding(jvmName = "PKCS1Padding"),
    NoPadding("NoPadding");
}
