package pw.binom.crypto

actual class Sha256MessageDigest : MessageDigest, AbstractJavaMessageDigest() {
    override val messageDigest = JMessageDigest.getInstance("SHA-256")!!
}