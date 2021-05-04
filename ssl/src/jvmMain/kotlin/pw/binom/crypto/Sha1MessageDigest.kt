package pw.binom.crypto

actual class Sha1MessageDigest : MessageDigest, AbstractJavaMessageDigest() {
    override val messageDigest = JMessageDigest.getInstance("SHA-1")!!
}