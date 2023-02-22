package pw.binom.crypto

import pw.binom.security.MessageDigest
import java.security.MessageDigest as JMessageDigest

actual class MD5MessageDigest : MessageDigest, AbstractJavaMessageDigest() {
    actual companion object;
    override val messageDigest = JMessageDigest.getInstance("MD5")!!
}
