package pw.binom.xml.serialization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import pw.binom.xml.XmlBuilder
import pw.binom.xml.serialization.annotations.XmlAttribute
import kotlin.test.Test

class DecodeTest {
  @Serializable
  @SerialName("Foo1")
  class Foo1(
    @XmlAttribute
    val name: String,
  )

  @Serializable
  sealed interface Foo2 {
    @Serializable
    @SerialName("SubClass1")
    data class SubClass1(
      @XmlAttribute val name: String,
    ) : Foo2

    @Serializable
    @SerialName("SubClass2")
    data class SubClass2(val name: String) : Foo2
  }

  @Serializable
  class Foo3(val name: String)

  @Test
  fun simple() {
    Foo1.serializer().deserialize(
      XmlDecoder2(
        serializersModule = EmptySerializersModule(),
        element = XmlBuilder.node("Foo1", "name" to "test-value"),
        config = XmlConfig(ignoreUnknownKey = false),
      )
    )
  }

  @Test
  fun sealed() {
    Foo2.serializer().deserialize(
      XmlDecoder2(
        serializersModule = EmptySerializersModule(),
        element = XmlBuilder.node("SubClass1", "name" to "test-value"),
        config = XmlConfig(ignoreUnknownKey = false),
      )
    )
  }

  @Test
  fun primitiveAsTag() {
    Foo3.serializer().deserialize(
      XmlDecoder2(
        serializersModule = EmptySerializersModule(),
        element = XmlBuilder.node("Foo3") {
          node("name") {
            text("value")
          }
        },
        config = XmlConfig(ignoreUnknownKey = false),
      )
    )
  }
}
