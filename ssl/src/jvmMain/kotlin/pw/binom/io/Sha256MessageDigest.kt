package pw.binom.io

import java.security.MessageDigest as JMessageDigest

actual class Sha256MessageDigest : MessageDigest, AbstractJavaMessageDigest() {
    override val messageDigest = JMessageDigest.getInstance("SHA-256")!!
}