package pw.binom.ssl

interface TrustManager {
    fun isClientTrusted(chain: Array<X509Certificate>?): Boolean
    fun isServerTrusted(chain: Array<X509Certificate>?): Boolean

    companion object {
        val TRUST_ALL = object : TrustManager {
            override fun isClientTrusted(chain: Array<X509Certificate>?): Boolean = true
            override fun isServerTrusted(chain: Array<X509Certificate>?): Boolean = true
        }
    }
}
