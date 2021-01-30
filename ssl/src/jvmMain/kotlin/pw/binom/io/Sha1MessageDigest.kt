package pw.binom.io

import java.security.MessageDigest as JMessageDigest

actual class Sha1MessageDigest : MessageDigest, AbstractJavaMessageDigest() {
    override val messageDigest = JMessageDigest.getInstance("SHA-1")!!
}