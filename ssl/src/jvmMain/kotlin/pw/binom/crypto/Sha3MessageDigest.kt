package pw.binom.crypto

import pw.binom.security.MessageDigest
import java.security.MessageDigest as JMessageDigest

actual class Sha3MessageDigest actual constructor(size: Size) : MessageDigest, AbstractJavaMessageDigest() {
    actual enum class Size(val value: String) {
        S224("224"),
        S254("254"),
        S256("256"),
        S384("384"),
        S512("512"),
    }

    override val messageDigest = JMessageDigest.getInstance("SHA3-${size.value}")!!
}
