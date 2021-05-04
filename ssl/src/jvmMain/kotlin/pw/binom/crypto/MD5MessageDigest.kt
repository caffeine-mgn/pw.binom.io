package pw.binom.crypto

actual class MD5MessageDigest : MessageDigest, AbstractJavaMessageDigest() {
    override val messageDigest = JMessageDigest.getInstance("MD5")!!
}