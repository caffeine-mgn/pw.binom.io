package pw.binom.wasm.writers

import pw.binom.copyTo
import pw.binom.io.ByteArrayOutput
import pw.binom.io.Input
import pw.binom.io.use
import pw.binom.wasm.*
import pw.binom.wasm.readers.DataSectionReader
import pw.binom.wasm.visitors.DataSectionVisitor
import pw.binom.wasm.visitors.ExpressionsVisitor

/**
 * https://webassembly.github.io/gc/core/binary/modules.html#binary-datasec
 */
class DataSectionWriter(private val out: WasmOutput) : DataSectionVisitor {

  private val stream = InMemoryWasmOutput()
  private var count = 0
  private var state = 0

  override fun active(): ExpressionsVisitor {
    check(state == 2)
    state++
    count++
    stream.i8u(DataSectionReader.ACTIVE_MEM_X)
    return ExpressionsWriter(stream)
  }

  override fun active(memoryId: MemoryId): ExpressionsVisitor {
    check(state == 2)
    state++
    count++
    stream.i8u(DataSectionReader.ACTIVE_MEM_0)
    stream.v32u(memoryId.raw)
    return ExpressionsWriter(stream)
  }

  override fun passive() {
    check(state == 2)
    state++
    count++
    stream.i8u(DataSectionReader.PASSIVE)
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
    out.write(stream)
    stream.clear()
  }
}
