package pw.binom.jwt

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import pw.binom.crypto.HMac
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.jvm.JvmInline

@JvmInline
value class JWTToken private constructor(private val raw: String) {
  @OptIn(ExperimentalEncodingApi::class)
  companion object {
    private fun String.removePadding() = removeSuffix("=").removeSuffix("=")

    private fun String.base64Encoded() = Base64.UrlSafe.encode(encodeToByteArray()).removeSuffix("=").removeSuffix("=")

    private sealed interface HMacShaAlg {
      val base64: String
      val bytes: ByteArray
      val type: HMac.AlgorithmType

      data object HS256 : HMacShaAlg {
        override val base64 = """{"typ":"JWT","alg":"HS256"}""".base64Encoded()
        override val bytes = base64.encodeToByteArray()
        override val type: HMac.AlgorithmType
          get() = HMac.AlgorithmType.SHA256
      }

      data object HS384 : HMacShaAlg {
        override val base64 = """{"typ":"JWT","alg":"HS384"}""".base64Encoded()
        override val bytes = base64.encodeToByteArray()
        override val type: HMac.AlgorithmType
          get() = HMac.AlgorithmType.SHA384
      }

      data object HS512 : HMacShaAlg {
        override val base64 =
          """{"typ":"JWT","alg":"HS512"}""".base64Encoded()
        override val bytes = base64.encodeToByteArray()
        override val type: HMac.AlgorithmType
          get() = HMac.AlgorithmType.SHA512
      }
    }

    private val dotEncoded = ".".encodeToByteArray()

    fun createHMac(
      alg: HMac.AlgorithmType,
      key: ByteArray,
      payload: JwtPayload,
    ): JWTToken {
      val algType =
        when (alg) {
          HMac.AlgorithmType.SHA256 -> HMacShaAlg.HS256
          HMac.AlgorithmType.SHA384 -> HMacShaAlg.HS384
          HMac.AlgorithmType.SHA512 -> HMacShaAlg.HS512
          else -> throw IllegalArgumentException("Alg $alg not supported yet")
        }
      val payloadBase64 = Base64.UrlSafe.encode(payload.bytes).removePadding()
      val hmac = HMac(algType.type, key)
      hmac.update(algType.bytes)
      hmac.update(dotEncoded)
      hmac.update(payloadBase64.encodeToByteArray())
      val signature = Base64.UrlSafe.encode(hmac.finish()).removePadding()
      return JWTToken("${algType.base64}.$payloadBase64.$signature")
    }

    private fun split(token: String): List<String> {
      val items = token.split('.', limit = 3)
      if (items.size != 3) {
        throw IllegalArgumentException("Invalid Jwt token: wrong part size")
      }
      check(items[2].count { it == '.' } == 0) { "Invalid signature" }
      return items
    }

    fun parse(token: String): JWTToken {
      require(token.count { it == '.' } == 2) { "Invalid token" }
      return JWTToken(token)
    }

    fun parse(
      token: String,
      verification: JetVerification,
    ): JWTToken? {
      val items = split(token)
      val head = Base64.UrlSafe.decode(items[0])
      val element = Json.decodeFromString(JsonElement.serializer(), head.decodeToString())
      val alg = JetAlgorithm.getByName(element.jsonObject["alg"]!!.jsonPrimitive.content)
      if (verification.verify(
          alg = alg,
          headBase64 = items[0],
          payloadBase64 = items[1],
          signBase64 = items[2],
        )
      ) {
        return null
      }
      return JWTToken(token)
    }
  }

  @OptIn(ExperimentalEncodingApi::class)
  val payload: JwtPayload
    get() {
      val items = split(raw)
      return JwtPayload(Base64.UrlSafe.decode(items[1]))
    }
  val token
    get() = raw

  override fun toString(): String = raw
}
