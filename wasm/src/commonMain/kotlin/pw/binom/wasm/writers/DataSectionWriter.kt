package pw.binom.wasm.writers

import pw.binom.copyTo
import pw.binom.io.ByteArrayOutput
import pw.binom.io.Input
import pw.binom.io.use
import pw.binom.wasm.MemoryId
import pw.binom.wasm.StreamWriter
import pw.binom.wasm.readers.DataSectionReader
import pw.binom.wasm.visitors.DataSectionVisitor
import pw.binom.wasm.visitors.ExpressionsVisitor

/**
 * https://webassembly.github.io/gc/core/binary/modules.html#binary-datasec
 */
class DataSectionWriter(private val out: StreamWriter) : DataSectionVisitor {

  private val data = ByteArrayOutput()
  private val stream = StreamWriter(data)
  private var count = 0
  private var state = 0

  override fun active(): ExpressionsVisitor {
    check(state == 2)
    state++
    count++
    stream.write(DataSectionReader.ACTIVE_MEM_X)
    return ExpressionsWriter(stream)
  }

  override fun active(memoryId: MemoryId): ExpressionsVisitor {
    check(state == 2)
    state++
    count++
    stream.write(DataSectionReader.ACTIVE_MEM_0)
    stream.v32u(memoryId.raw)
    return ExpressionsWriter(stream)
  }

  override fun passive() {
    check(state == 2)
    state++
    count++
    stream.write(DataSectionReader.PASSIVE)
  }

  override fun data(input: Input) {
    check(state == 3)
    state++
    ByteArrayOutput().use { b ->
      input.copyTo(b)
      b.locked { buffer ->
        stream.v32u(buffer.remaining.toUInt())
        stream.write(buffer)
      }
    }
  }

  override fun elementStart() {
    check(state == 1)
    state++
  }

  override fun elementEnd() {
    check(state == 4)
    state = 1
  }

  override fun start() {
    check(state == 0)
    state++
  }

  override fun end() {
    out.v32u(count.toUInt())
    data.locked {
      out.write(it)
    }
    data.clear()
  }
}
