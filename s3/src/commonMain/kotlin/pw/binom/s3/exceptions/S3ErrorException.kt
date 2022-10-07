package pw.binom.s3.exceptions

class S3ErrorException(val code: String?, val description: String?) : S3Exception() {

    override val message: String?
        get() {
            if (code == null && description == null) {
                return null
            }
            val sb = StringBuilder()
            if (code != null) {
                sb.append(code)
            }
            if (code != null && description != null) {
                sb.append(": ")
            }
            if (description != null) {
                sb.append(description)
            }
            return sb.toString()
        }
}
