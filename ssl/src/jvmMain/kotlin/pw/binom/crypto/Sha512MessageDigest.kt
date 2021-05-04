package pw.binom.crypto

actual class Sha512MessageDigest : MessageDigest, AbstractJavaMessageDigest() {
    override val messageDigest = JMessageDigest.getInstance("SHA-512")!!
}