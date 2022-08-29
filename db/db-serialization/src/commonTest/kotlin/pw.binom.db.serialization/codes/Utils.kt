package pw.binom.db.serialization.codes

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import pw.binom.db.serialization.DataBinder
import pw.binom.db.serialization.DataProvider
import kotlin.jvm.JvmName
import kotlin.test.Test

fun Map<String, Any?>.toDataBinder() = object : DataProvider {
    override fun get(key: String): Any? = this@toDataBinder[key]

    override fun contains(key: String): Boolean = this@toDataBinder.containsKey(key)
}

@JvmName("toMutableDataBinder")
fun MutableMap<String, Any?>.toMutableDataBinder() = object : DataBinder {
    override fun get(key: String): Any? = this@toMutableDataBinder[key]

    override fun contains(key: String): Boolean = this@toMutableDataBinder.containsKey(key)
    override fun set(key: String, value: Any?) {
        this@toMutableDataBinder[key] = value
    }
}

class AAAA {
    @Test
    fun aaa() = runTest {
        val flow = flow<String> {
            println("Start flow")
            repeat(10) {
                try {
                    emit("->$it")
                } catch (e: CancellationException) {
                    // Do nothing
                    return@flow
                } catch (e: Throwable) {
                    println("->${e::class}")
                    e.printStackTrace()
                }
            }
        }

        println("->${flow.first()}")
        flow.collect { b ->
            println("->$b")
        }
    }
}
