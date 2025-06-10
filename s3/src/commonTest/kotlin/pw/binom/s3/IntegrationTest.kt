package pw.binom.s3

import pw.binom.http.client.HttpClientRunnable
import pw.binom.http.client.factory.NativeNetChannelFactory
import pw.binom.network.MultiFixedSizeThreadNetworkDispatcher
import pw.binom.url.toURL
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class IntegrationTest {
  private var networkManager: MultiFixedSizeThreadNetworkDispatcher? = null
  private var internalHttpClient: HttpClientRunnable? = null
  private var internalClient: S3ClientImpl? = null

  protected val httpClient: HttpClientRunnable
    get() = internalHttpClient!!

  protected val s3: S3Client
    get() = internalClient!!

  @BeforeTest
  fun setup() {
    networkManager = MultiFixedSizeThreadNetworkDispatcher(4)
    internalHttpClient = HttpClientRunnable(source = NativeNetChannelFactory(networkManager!!))
    internalClient =
      S3ClientImpl(
        url = "http://127.0.0.1:7122/".toURL(),
        accessKey = "accessKey1",
        secretAccessKey = "verySecretKey1",
        client = internalHttpClient!!,
      )
  }

  @AfterTest
  fun shutdown() {
    networkManager?.close()
  }
}
