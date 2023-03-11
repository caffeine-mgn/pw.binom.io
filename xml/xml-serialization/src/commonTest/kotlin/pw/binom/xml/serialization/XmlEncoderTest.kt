package pw.binom.xml.serialization

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import pw.binom.io.asAsync
import pw.binom.xml.dom.XmlElement
import pw.binom.xml.sax.AsyncXmlRootWriterVisitor
import pw.binom.xml.serialization.annotations.XmlName
import pw.binom.xml.serialization.annotations.XmlNamespace
import pw.binom.xml.serialization.annotations.XmlNode
import pw.binom.xml.serialization.annotations.XmlWrapper
import kotlin.test.Ignore
import kotlin.test.Test

@Serializable
@XmlNamespace(["https://1"])
@SerialName("data")
data class TestData(
    @XmlNamespace(["https://1"])
    val name: String,

    @XmlNamespace(["https://1"])
    val value: TestData2,

    @XmlWrapper("names")
    @XmlName("value")
    val names: List<String>,
)

@Serializable
@SerialName("data2")
data class TestData2(
    @XmlNamespace(["https://1"]) @XmlNode
    val age: Int,
)

@Polymorphic
@Serializable
@SerialName("PolimorfClass")
data class PolimorfClass(val ololo: String)

class XmlEncoderTest {

    @Test
    fun ggg() {
        val number = 144
        val signed = number.toByte()
        val unsigned = number.toUByte()
        println("signed=$signed ${signed.toString(2)}")
        println("unsigned=$unsigned ${unsigned.toString(2)}")
        println("signed->unsigned=${signed.toUByte()} ${signed.toUByte().toString(2)}")
        println("unsigned->signed=${unsigned.toByte()} ${unsigned.toByte().toString(2)}")
    }

    @Test
    @Ignore
    fun test() = runTest {
        val module = SerializersModule {
            polymorphic(Any::class, PolimorfClass::class, PolimorfClass.serializer())
        }
        val oo = TestData("Hello!", TestData2(32), listOf("Anton", "Masha"))
        val xx = Xml(module).encodeToXmlElement(TestData.serializer(), oo)
        val sb = StringBuilder()
        val root = XmlElement()
        xx.parent = root
        root.accept(AsyncXmlRootWriterVisitor.withHeader(sb.asAsync()))
        val vv = Xml(module).decodeFromXmlElement(TestData.serializer(), xx)
        println("Before: $oo")
        println("After: $vv")

        println("Xml:\n$sb")
    }
}
