package pw.binom.crypto

import pw.binom.io.MessageDigest
import java.security.MessageDigest as JMessageDigest

actual class MD5MessageDigest : MessageDigest, AbstractJavaMessageDigest() {
    override val messageDigest = JMessageDigest.getInstance("MD5")!!
}
