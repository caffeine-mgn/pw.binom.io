package pw.binom.io

import java.security.MessageDigest as JMessageDigest

actual class MD5 : MessageDigest, AbstractJavaMessageDigest() {
    override val messageDigest = JMessageDigest.getInstance("MD5")!!
}