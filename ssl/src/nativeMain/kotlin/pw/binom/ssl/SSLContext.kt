package pw.binom.ssl

import kotlinx.cinterop.*
import platform.openssl.*
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.socket.ssl.SSLSession

private val inited = AtomicBoolean(false)
internal val chiperList =
  "TLS_AES_256_GCM_SHA384:TLS_CHACHA20_POLY1305_SHA256:TLS_AES_128_GCM_SHA256:DHE-RSA-AES256-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384:DHE-DSS-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384:ECDHE-ECDSA-CHACHA20-POLY1305:ECDHE-RSA-CHACHA20-POLY1305:DHE-RSA-CHACHA20-POLY1305:ECDHE-ECDSA-AES256-CCM8:ECDHE-ECDSA-AES256-CCM:DHE-RSA-AES256-CCM8:DHE-RSA-AES256-CCM:ECDHE-ECDSA-ARIA256-GCM-SHA384:ECDHE-ARIA256-GCM-SHA384:DHE-DSS-ARIA256-GCM-SHA384:DHE-RSA-ARIA256-GCM-SHA384:ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:DHE-DSS-AES128-GCM-SHA256:DHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES128-CCM8:ECDHE-ECDSA-AES128-CCM:DHE-RSA-AES128-CCM8:DHE-RSA-AES128-CCM:ECDHE-ECDSA-ARIA128-GCM-SHA256:ECDHE-ARIA128-GCM-SHA256:DHE-DSS-ARIA128-GCM-SHA256:DHE-RSA-ARIA128-GCM-SHA256:ECDHE-ECDSA-AES256-SHA384:ECDHE-RSA-AES256-SHA384:DHE-DSS-AES256-SHA256:ECDHE-ECDSA-CAMELLIA256-SHA384:ECDHE-RSA-CAMELLIA256-SHA384:DHE-RSA-CAMELLIA256-SHA256:DHE-DSS-CAMELLIA256-SHA256:ECDHE-ECDSA-AES128-SHA256:ECDHE-RSA-AES128-SHA256:DHE-RSA-AES128-SHA256:DHE-DSS-AES128-SHA256:ECDHE-ECDSA-CAMELLIA128-SHA256:ECDHE-RSA-CAMELLIA128-SHA256:DHE-RSA-CAMELLIA128-SHA256:DHE-DSS-CAMELLIA128-SHA256:ECDHE-ECDSA-AES256-SHA:ECDHE-RSA-AES256-SHA:DHE-RSA-AES256-SHA:DHE-DSS-AES256-SHA:DHE-RSA-CAMELLIA256-SHA:DHE-DSS-CAMELLIA256-SHA:ECDHE-ECDSA-AES128-SHA:ECDHE-RSA-AES128-SHA:DHE-RSA-AES128-SHA:DHE-DSS-AES128-SHA:DHE-RSA-CAMELLIA128-SHA:DHE-DSS-CAMELLIA128-SHA"

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
actual class SSLContext(method: SSLMethod, val keyManager: KeyManager, val trustManager: TrustManager) {

  init {
    if (inited.compareAndSet(false, true)) {
      OPENSSL_init_crypto(OPENSSL_INIT_ADD_ALL_CIPHERS.convert(), null)
      OPENSSL_init_crypto(OPENSSL_INIT_ADD_ALL_DIGESTS.convert(), null)
      OPENSSL_init_ssl(0.convert(), null)
    }
  }

//    actual val socketFactory: SSLSocketFactory by lazy { SSLSocketFactory(this) }

  private val client_method: CPointer<SSL_METHOD> = when (method) {
    SSLMethod.TLS, SSLMethod.TLSv1_2 -> TLSv1_2_client_method()
    SSLMethod.TLSv1 -> TLSv1_client_method()
    SSLMethod.TLSv1_1 -> TLSv1_1_client_method()
    else -> TODO()
  }!!
  private val server_method: CPointer<SSL_METHOD> = when (method) {
    SSLMethod.TLS, SSLMethod.TLSv1_2 -> TLSv1_2_server_method()
    SSLMethod.TLSv1 -> TLSv1_server_method()
    SSLMethod.TLSv1_1 -> TLSv1_1_server_method()
    else -> TODO()
  }!!

  actual companion object {
    actual fun getInstance(method: SSLMethod, keyManager: KeyManager, trustManager: TrustManager): SSLContext =
      SSLContext(method, keyManager, trustManager)
  }

  actual fun clientSession(host: String, port: Int): SSLSession {
    val sslCtx = SSL_CTX_new(client_method)!!
    SSL_CTX_set_verify(sslCtx, SSL_VERIFY_NONE, null)
    val ssl = SSL_new(sslCtx)!!
    val connect = "$host:$port"
    if (SSL_set1_host(ssl, connect) <= 0) {
      throw RuntimeException("Can't set SSL host to [$connect]")
    }
    if (SSL_ctrl(ssl, SSL_CTRL_SET_TLSEXT_HOSTNAME, TLSEXT_NAMETYPE_host_name.convert(), connect.cstr) <= 0) {
      throw RuntimeException("Can't set SSL tlsext_hostname to [$connect]")
    }
    return SSLSession(ctx = sslCtx, ssl = ssl, client = true, trustManager = trustManager, keyManager = keyManager)
  }

  actual fun serverSession(): SSLSession {
    val sslCtx = SSL_CTX_new(server_method)!!
    SSL_CTX_set_verify(sslCtx, SSL_VERIFY_NONE, null)
    val ssl = SSL_new(sslCtx)!!
    return SSLSession(
      ctx = sslCtx,
      ssl = ssl,
      client = false,
      trustManager = trustManager,
      keyManager = keyManager,
    )
  }
}
