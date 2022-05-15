package pw.binom.crypto

import pw.binom.security.MessageDigest
import java.security.MessageDigest as JMessageDigest

actual class Sha512MessageDigest : MessageDigest, AbstractJavaMessageDigest() {
    override val messageDigest = JMessageDigest.getInstance("SHA-512")!!
}
