package pw.binom.http.rest

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pw.binom.http.rest.annotations.EndpointMapping
import pw.binom.http.rest.annotations.GetParam
import pw.binom.http.rest.annotations.HeaderParam
import pw.binom.http.rest.annotations.PathParam
import pw.binom.http.rest.endpoints.EndpointWithNothing
import pw.binom.url.toURI
import kotlin.test.Test
import kotlin.test.assertEquals

class HttpInputDecoderTest {

  object GUIDSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor
      get() = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): String = decoder.decodeString().removePrefix("{").removeSuffix("}")

    override fun serialize(encoder: Encoder, value: String) {
      encoder.encodeString("{$value}")
    }
  }

  @Serializable
  enum class Type {
    ON,
    OFF
  }

  @Serializable
  @EndpointMapping(method = "GET", path = "/users/{id}")
  data class GetUser(
    @PathParam
    val id: String,
    @HeaderParam("X-Trace-Id")
    val trace: String,
    @HeaderParam("X-uuid")
    @Serializable(GUIDSerializer::class)
    val uuid: String,
    @GetParam
    val search: String? = null,
    @GetParam
    val type1: Type,
    @GetParam
    val type2: Type,

    @HeaderParam("X-uuid-2")
    @Serializable(GUIDSerializer::class)
    val uuid2: String = UUID2_DEFAULT_VALUE,
  ) : EndpointWithNothing {
    companion object {
      const val UUID2_DEFAULT_VALUE = "none"
    }
  }

  @Test
  fun decodeTest() {
    val uuid = "4d67e25d-b76f-43d0-b850-7864a0c0f161"
    val traceId = "23424234"
    val search = "hello"
    val id = "28"
    val decoder = HttpInputDecoder()
    val type1 = Type.ON
    val type2 = Type.OFF
    decoder.reset(
      description = EndpointDescription.create(GetUser.serializer()),
      input = StubHttpServerExchange(
        requestMethod = "GET",
        requestURI = "/users/$id".toURI()
          .appendQuery("search", search)
          .appendQuery("type1", type1.toString())
          .appendQuery("type2", type2.toString())
      )
        .header("X-Trace-Id", traceId)
        .header("x-uuid", "{$uuid}")
    )
    val a = GetUser.serializer().deserialize(decoder)
    assertEquals(id, a.id)
    assertEquals(search, a.search)
    assertEquals(traceId, a.trace)
    assertEquals(uuid, a.uuid)
    assertEquals(GetUser.UUID2_DEFAULT_VALUE, a.uuid2)
    assertEquals(type1, a.type1)
    assertEquals(type2, a.type2)
  }
}
