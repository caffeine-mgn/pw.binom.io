package pw.binom.db.serialization.codes

import kotlinx.serialization.Serializable
import pw.binom.db.serialization.DefaultSQLSerializePool
import pw.binom.db.serialization.EmbeddedSplitter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EmbeddedTest {
    @Serializable
    data class Root(
        val a: String,
        @EmbeddedSplitter("_")
        val s: Subclass,
        @EmbeddedSplitter("_")
        val d: Subclass?,
        @EmbeddedSplitter("_")
        val n: SubclassNullable?,
        @EmbeddedSplitter("_")
        val m: SubclassNullable?,
        @EmbeddedSplitter("_")
        val k: SubclassNullable2?,
    )

    @Serializable
    data class Subclass(val b: String)

    @Serializable
    data class SubclassNullable(val b: String?)

    @Serializable
    data class SubclassNullable2(val h: String?, val m: String?)

    @Test
    fun encodeEmbedded() {
        val result = HashMap<String, Any?>()
        DefaultSQLSerializePool.encode(
            serializer = Root.serializer(),
            value = Root(
                a = "a",
                s = Subclass(b = "b"),
                d = null,
                n = SubclassNullable(b = null),
                m = null,
                k = SubclassNullable2(h = null, m = null)
            ),
            name = "",
            output = result.toMutableDataBinder(),
            useQuotes = false,
            excludeGenerated = false,
        )
        assertNull(result["n_b"])
        assertEquals("a", result["a"])
        assertNull(result["d"])
        assertEquals("b", result["s_b"])
        assertNull(result["m"])
        assertNull(result["k_m"])
        assertNull(result["k_h"])
        assertEquals(7, result.size)
    }

    @Test
    fun decodeEmbedded() {
        val data = hashMapOf<String, Any?>("n_b" to null, "a" to "a", "d" to null, "s_b" to "b", "m" to null)
        val d = DefaultSQLSerializePool.decode(
            serializer = Root.serializer(),
            name = "",
            input = data.toMutableDataBinder(),
        )
        println("d=$d")
    }
}
