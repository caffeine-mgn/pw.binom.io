package pw.binom.io

import java.security.MessageDigest as JMessageDigest

actual class Sha512 : MessageDigest, AbstractJavaMessageDigest() {
    override val messageDigest = JMessageDigest.getInstance("SHA-512")!!
}