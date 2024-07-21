package pw.binom.process

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.convert
import pw.binom.io.ByteBuffer
import pw.binom.io.Output

//@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
//class PipeOutput : Pipe(), Output {
//
//  override fun write(data: ByteBuffer): Int {
//    val wrote = data.ref(0) { dataPtr, remaining ->
//      if (remaining > 0) {
//        platform.posix.write(write, dataPtr, remaining.convert()).convert<Int>()
//      } else {
//        0
//      }
//    }
//    data.position += wrote
//    return wrote
//  }
//
//  override fun flush() {
//  }
//
//  override fun close() {
//  }
//}
