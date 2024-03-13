package pw.binom.properties

import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.EmptySerializersModule
import pw.binom.properties.serialization.PropertiesDecoder
import pw.binom.properties.serialization.annotations.PropertiesPrefix
import kotlin.test.Test
import kotlin.test.assertEquals

class PropertiesDecoderTest {
  @Serializable
  enum class Type {
    A,
    B,
  }

  @Serializable
  data class Auth(val name: String, val pass: String)

  @Serializable
  data class Config(val auth: Auth, val ports: List<Int?>, val type: Type)

  @PropertiesPrefix("main")
  @Serializable
  data class ConfigWithPrefix(val type: Type)

  @Test
  fun prefixTest() {
    val properties =
      IniParser.parseMap(
        mapOf(
          "type" to "A",
          "main.type" to "B",
        ),
      )
    val decoder =
      PropertiesDecoder(
        root = properties,
        serializersModule = EmptySerializersModule(),
        prefix = "",
      )
    val decodedConfig = ConfigWithPrefix.serializer().deserialize(decoder)

    assertEquals(
      ConfigWithPrefix(
        type = Type.B,
      ),
      decodedConfig,
    )
  }

  @Test
  fun mainTest() {
    val properties =
      IniParser.parseMap(
        mapOf(
          "ports[0]" to "11",
          "ports[2]" to "12",
          "auth.name" to "admin",
          "auth.pass" to "password",
          "type" to "A",
        ),
      )
    val decoder =
      PropertiesDecoder(
        root = properties,
        serializersModule = EmptySerializersModule(),
        prefix = "",
      )
    val decodedConfig = Config.serializer().deserialize(decoder)

    assertEquals(
      Config(
        auth = Auth(name = "admin", pass = "password"),
        ports = listOf(11, null, 12),
        type = Type.A,
      ),
      decodedConfig,
    )
  }
}
