package pw.binom.wasm.writers

import pw.binom.wasm.InMemoryWasmOutput
import pw.binom.wasm.TypeId
import pw.binom.wasm.WasmOutput
import pw.binom.wasm.readers.TypeSectionReader
import pw.binom.wasm.visitors.StorageVisitor
import pw.binom.wasm.visitors.TypeSectionVisitor
import pw.binom.wasm.visitors.ValueVisitor

class TypeSectionWriter(private val out: WasmOutput) : TypeSectionVisitor {
  class FuncTypeWriter(private val out: WasmOutput) : TypeSectionVisitor.FuncTypeVisitor {
    companion object {
      private const val NONE = 0
      private const val START = 1
      private const val ARG = 2
      private const val RESULT = 3
    }

    private var status = 0
    private var count = 0
    private val stream = InMemoryWasmOutput()
    override fun start(shared: Boolean) {
      check(status == NONE)
      status = START
      if (shared) {
        out.i8u(TypeSectionReader.kSharedFlagCode)
      }
      out.i8u(TypeSectionReader.kWasmFunctionTypeCode)
    }

    override fun end() {
      check(status == START || status == ARG || status == RESULT)
      when (status) {
        START -> {
          out.v32u(0u)
          out.v32u(0u)
        }

        ARG -> {
          stream.v32u(count.toUInt())
          stream.moveTo(out)
          stream.v32u(0u)
        }

        RESULT -> {
          stream.v32u(count.toUInt())
          stream.moveTo(out)
        }
      }
      count = 0
      status = NONE
    }

    override fun arg(): ValueVisitor {
      check(status == START || status == ARG)
      if (status == START) {
        status = ARG
      }
      count++
      return ValueWriter(stream)
    }

    override fun result(): ValueVisitor {
      check(status == START || status == ARG || status == RESULT)
      when (status) {
        START -> {
          out.v32u(0u)
          status = RESULT
        }

        ARG -> {
          status = RESULT
          stream.v32u(count.toUInt())
          stream.moveTo(out)
          count = 0
        }
      }
      count++
      return ValueWriter(stream)
    }
  }

  class StructTypeWriter(private val out: WasmOutput) : TypeSectionVisitor.StructTypeVisitor {
    private val stream = InMemoryWasmOutput()
    private var count = 0

    override fun start(shared: Boolean) {
      if (shared) {
        out.i8u(TypeSectionReader.kSharedFlagCode)
      }
      out.i8u(TypeSectionReader.kWasmStructTypeCode)
    }

    override fun fieldStart(): StorageVisitor {
      count++
      return StorageWriter(stream)
    }

    override fun fieldEnd(mutable: Boolean) {
      stream.v1u(mutable)
    }

    override fun end() {
      out.v32u(count.toUInt())
      stream.moveTo(out)
    }
  }

  class ArrayWriter(private val out: WasmOutput) : TypeSectionVisitor.ArrayVisitor {
    private var state = 0
    override fun start(shared: Boolean) {
      check(state == 0)
      state++
      if (shared) {
        out.i8u(TypeSectionReader.kSharedFlagCode)
      }
      out.i8u(TypeSectionReader.kWasmArrayTypeCode)
    }

    override fun type(): StorageVisitor {
      check(state == 1)
      state++
      return StorageWriter(out)
    }

    override fun mutable(value: Boolean) {
      check(state == 2)
      state++
      out.v1u(value)
    }

    override fun end() {
      check(state == 3)
      state = 0
    }
  }

  class CompositeTypeWriter(private val out: WasmOutput) : TypeSectionVisitor.CompositeTypeVisitor {
    override fun array(): TypeSectionVisitor.ArrayVisitor = ArrayWriter(out)

    override fun function(): TypeSectionVisitor.FuncTypeVisitor = FuncTypeWriter(out)

    override fun struct(): TypeSectionVisitor.StructTypeVisitor = StructTypeWriter(out)
  }

  class SubTypeWithParentWriter(private val out: WasmOutput) : TypeSectionVisitor.SubTypeWithParentVisitor {
    private val types = ArrayList<Int>()
    private var state = 0
    override fun start() {
      check(state == 0)
      state++
      super.start()
    }

    override fun parent(type: TypeId) {
      check(state == 1)
      types += type.value.toInt()
    }

    override fun type(): TypeSectionVisitor.CompositeTypeVisitor {
      check(state == 1)
      state++
      out.v32u(types.size.toUInt())
      types.forEach {
        out.v32u(it.toUInt())
      }
      types.clear()
      return CompositeTypeWriter(out)
    }

    override fun end() {
      check(state == 2)
      state = 0
    }
  }

  class SubTypeWriter(private val out: WasmOutput) : TypeSectionVisitor.SubTypeVisitor {
    override fun withParent(): TypeSectionVisitor.SubTypeWithParentVisitor {
      out.i8u(TypeSectionReader.kWasmSubtypeCode)
      return SubTypeWithParentWriter(out)
    }

    override fun withParentFinal(): TypeSectionVisitor.SubTypeWithParentVisitor {
      out.i8u(TypeSectionReader.kWasmSubtypeFinalCode)
      return SubTypeWithParentWriter(out)
    }

    override fun single(): TypeSectionVisitor.CompositeTypeVisitor {
      return CompositeTypeWriter(out)
    }
  }

  class RecursiveWriter(private val out: WasmOutput) : TypeSectionVisitor.RecursiveVisitor {
    private var count = 0
    private val stream = InMemoryWasmOutput()
    private var state = 0
    override fun start() {
      check(state == 0)
      state++
      super.start()
    }

    override fun type(): TypeSectionVisitor.SubTypeVisitor {
      check(state == 0 || state == 1)
      state = 1
      count++
      return SubTypeWriter(out)
    }

    override fun end() {
      check(state == 0 || state == 1)
      state = 0
      out.v32s(count)
      stream.moveTo(out)
      count = 0
    }
  }

  class RecTypeWriter(private val out: WasmOutput) : TypeSectionVisitor.RecTypeVisitor {
    override fun recursive(): TypeSectionVisitor.RecursiveVisitor {
      out.i8u(TypeSectionReader.kWasmRecursiveTypeGroupCode)
      return RecursiveWriter(out)
    }

    override fun single(): TypeSectionVisitor.SubTypeVisitor {
      return SubTypeWriter(out)
    }
  }

}
