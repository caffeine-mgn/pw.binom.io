package pw.binom.io.socket
/*

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.pointed
import platform.common.*
import platform.socket.*
import pw.binom.io.InHeap

@OptIn(ExperimentalForeignApi::class)
abstract class AbstractMutableInetNetworkSocketAddress : MutableInetNetworkSocketAddress {
  val nativeData = InHeap.create<NInetSocketNetworkAddress>()

  //    @OptIn(ExperimentalStdlibApi::class)
//    private val clearer = createCleaner(nativeData) {
//        nativeHeap.free(it)
//    }
//    val data
//        get() = nativeData.data

  //    val data = ByteArray(28)
  var size
    set(value) {
      nativeData.use {
        it.pointed.size = value
      }
    }
    get() = nativeData.use { it.pointed.size }

  protected var hashCode = 0

  override fun hashCode(): Int = hashCode

  protected fun refreshHashCode(host: String, port: Int) {
    var hashCode = host.hashCode()
    hashCode = 31 * hashCode + port.hashCode()
    this.hashCode = hashCode
  }

  internal inline fun <T> addr(func: (CPointer<ByteVar>) -> T): T =
    nativeData.use {
      func(it.pointed.data)
    }

  override fun toMutable(): MutableInetNetworkSocketAddress = this

  override fun toString(): String = "$host:$port"

  override fun toMutable(dest: MutableInetNetworkSocketAddress): MutableInetNetworkSocketAddress {
    if (dest is AbstractMutableInetNetworkSocketAddress) {
      this.nativeData.copyInto(dest.nativeData)
    } else {
      dest.update(
        host = host,
        port = port,
      )
    }
    return dest
  }
}
*/
