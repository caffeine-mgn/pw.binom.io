package pw.binom.http.rest

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pw.binom.http.rest.annotations.*
import pw.binom.http.rest.endpoints.EndpointWithNothing
import pw.binom.http.rest.serialization.HttpOutputEncoder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HttpOutputEncoderTest {
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
    val search1: String? = null,
    @GetParam
    val search2: String? = null,
    @GetParam
    val type1: Type,
    @GetParam
    val type2: Type,

    @HeaderParam("X-uuid-2")
    @Serializable(GUIDSerializer::class)
    val uuid2: String = UUID2_DEFAULT_VALUE,

    @BodyParam
    val body: String,
  ) : EndpointWithNothing {
    companion object {
      const val UUID2_DEFAULT_VALUE = "none"
    }
  }

  @Test
  fun test() {
    val id = "ewrwer"
    val trace = "eewe"
    val uuid = "fdfd"
    val search1 = "ddww"
    val body = "345345435"
    val encoder = HttpOutputEncoder(endpointDescription = EndpointDescription.create(GetUser.serializer()))
    GetUser.serializer().serialize(
      encoder, GetUser(
        id = id,
        trace = trace,
        uuid = uuid,
        search1 = search1,
        search2 = null,
        type1 = Type.ON,
        type2 = Type.OFF,
        body = body
      )
    )
    assertEquals(1, encoder.pathParams.size)
    assertEquals(id, encoder.pathParams["id"])

    assertEquals(3, encoder.getParams.size)
    assertEquals("ON", encoder.getParams["type1"])
    assertEquals("OFF", encoder.getParams["type2"])
    assertEquals(search1, encoder.getParams["search1"])

    assertEquals(body, encoder.body!!.body)
    assertTrue(String.serializer() == encoder.body!!.serializer)
    assertEquals(3, encoder.headerParams.size)
    assertEquals(trace, encoder.headerParams["x-trace-id"]!!.single())
    assertEquals("{$uuid}", encoder.headerParams["x-uuid"]!!.single())
    assertEquals("{none}", encoder.headerParams["x-uuid-2"]!!.single())
  }
}
