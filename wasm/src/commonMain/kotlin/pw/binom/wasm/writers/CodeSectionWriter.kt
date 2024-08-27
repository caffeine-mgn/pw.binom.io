package pw.binom.wasm.writers

import pw.binom.io.ByteArrayOutput
import pw.binom.wasm.InMemoryWasmOutput
import pw.binom.wasm.StreamWriter
import pw.binom.wasm.WasmOutput
import pw.binom.wasm.visitors.CodeSectionVisitor
import pw.binom.wasm.visitors.ExpressionsVisitor
import pw.binom.wasm.visitors.ValueVisitor

class CodeSectionWriter(private val out: WasmOutput) : CodeSectionVisitor {

  class CodeWriter(private val out: WasmOutput) : CodeSectionVisitor.CodeVisitor {
    companion object {
      private const val NONE = 0
      private const val STARTED = 1
      private const val LOCAL_START = 2
      private const val CODE_START = 3
    }

    private var state = 0
    private var localCount = 0

    private val localStream = InMemoryWasmOutput()
    private val codeStream = InMemoryWasmOutput()

    override fun start() {
      check(state == NONE)
      state = STARTED
    }

    override fun end() {
      when (state) {
        STARTED -> out.v32u(0u) // size of block is 0
        LOCAL_START -> {
          val localCountStream = InMemoryWasmOutput()
          localCountStream.v32u(localCount.toUInt())
          out.v32u((localCountStream.size + localStream.size).toUInt()) // size of block
          localCountStream.moveTo(out)
          localStream.moveTo(out)
        }

        CODE_START -> {
          val localCountStream = InMemoryWasmOutput()
          localCountStream.v32u(localCount.toUInt())
          out.v32u((localCountStream.size + localStream.size + codeStream.size).toUInt()) // size of block
          localCountStream.moveTo(out)
          localStream.moveTo(out)
          codeStream.moveTo(out)
        }

        else -> throw IllegalStateException()
      }
      state = NONE
      localCount = 0
      localStream.clear()
      codeStream.clear()
    }

    override fun local(size: UInt): ValueVisitor {
      if (localCount == 0) {
        check(state == STARTED)
        state = LOCAL_START
      } else {
        check(state == LOCAL_START)
      }
      localCount++
      return ValueWriter(localStream)
    }


    override fun code(): ExpressionsVisitor {
      check(state == STARTED || state == LOCAL_START)
      state = CODE_START
      return ExpressionsWriter(codeStream)
    }
  }

  private var state = 0
  private var count = 0
  private val data = InMemoryWasmOutput()
  override fun start() {
    check(state == 0)
    state++
  }

  override fun code(): CodeSectionVisitor.CodeVisitor {
    check(state == 1)
    count++
    return CodeWriter(out)
  }

  override fun end() {
    check(state == 1)
    out.v32u(count.toUInt())
    data.moveTo(out)
    state = 0
  }
}
