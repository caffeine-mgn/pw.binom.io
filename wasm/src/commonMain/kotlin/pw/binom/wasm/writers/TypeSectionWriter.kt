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

    override fun arg(): ValueVisitor {
      check(status == START || status == ARG)
      status = ARG
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
          out.v32u(count.toUInt())
          stream.moveTo(out)
          count = 0
        }
      }
      count++
      return ValueWriter(stream)
    }

    override fun end() {
      check(status == START || status == ARG || status == RESULT)
      when (status) {
        START -> {
          out.v32u(0u)
          out.v32u(0u)
        }

        ARG -> {
          out.v32u(count.toUInt())
          stream.moveTo(out)
          out.v32u(0u)
        }

        RESULT -> {
          out.v32u(count.toUInt())
          stream.moveTo(out)
        }
      }
      count = 0
      status = NONE
    }
  }

  class StructTypeWriter(private val out: WasmOutput) : TypeSectionVisitor.StructTypeVisitor {
    private val stream = InMemoryWasmOutput()
    private var count = 0
    private var status = 0

    override fun start(shared: Boolean) {
      check(status == 0)
      status++
      if (shared) {
        out.i8u(TypeSectionReader.kSharedFlagCode)
      }
      out.i8u(TypeSectionReader.kWasmStructTypeCode)
    }

    private var cursor = 0
    override fun fieldStart(): StorageVisitor {
      check(status == 1)
      status++
      count++
      cursor = stream.size
      return StorageWriter(stream)
    }

    override fun fieldEnd(mutable: Boolean) {
      check(status == 2)
      status--
      println("before write mutable. size of type: ${stream.size - cursor}")
      cursor = stream.size
      stream.v1u(mutable)
      println("v1u size: ${stream.size - cursor}")
    }

    override fun end() {
      check(status == 1)
      out.v32u(count.toUInt())
      stream.moveTo(out)
      status = 0
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
    companion object {
      private const val NONE = 0
      private const val STARTED = 1
      private const val PARENT = 2
      private const val TYPE = 3
    }

    private val types = ArrayList<Int>()
    private var state = 0
    override fun start() {
      check(state == NONE)
      state = STARTED
      super.start()
    }

    override fun parent(type: TypeId) {
      check(state == STARTED || state == PARENT)
      state = PARENT
      types += type.value.toInt()
    }

    override fun type(): TypeSectionVisitor.CompositeTypeVisitor {
      check(state == STARTED || state == PARENT) { "Invalid state: $state" }
      state = TYPE
      out.v32u(types.size.toUInt())
      types.forEach {
        out.v32u(it.toUInt())
      }
      types.clear()
      return CompositeTypeWriter(out)
    }

    override fun end() {
      check(state == TYPE)
      state = NONE
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

    override fun single(): TypeSectionVisitor.CompositeTypeVisitor = CompositeTypeWriter(out)
  }

  class RecursiveWriter(private val out: WasmOutput) : TypeSectionVisitor.RecursiveVisitor {
    private var count = 0
    private val stream = InMemoryWasmOutput()
    private var state = 0
    override fun start() {
      check(state == 0)
      state++
      out.i8u(TypeSectionReader.kWasmRecursiveTypeGroupCode)
    }

    override fun type(): TypeSectionVisitor.SubTypeVisitor {
      check(state == 0 || state == 1)
      state = 1
      count++
      return SubTypeWriter(stream)
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
    override fun recursive(): TypeSectionVisitor.RecursiveVisitor = RecursiveWriter(out)

    override fun single(): TypeSectionVisitor.SubTypeVisitor = SubTypeWriter(out)
  }

  private var count = 0
  private var buffer = InMemoryWasmOutput()
  private var status = 0
  override fun start() {
    check(status == 0)
    status++
  }

  override fun recType(): TypeSectionVisitor.RecTypeVisitor {
    check(status == 1)
    count++
    return RecTypeWriter(buffer)
  }

  override fun end() {
    check(status == 1)
    out.v32u(count.toUInt())
    buffer.moveTo(out)
    status = 0
  }
}
