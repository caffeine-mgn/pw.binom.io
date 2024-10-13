package pw.binom.wasm.runner

interface GlobalVar {
  val s32: Int
    get() = TODO()
  val u32: UInt
    get() = TODO()
  val u64: ULong
    get() = TODO()
  val s64: Long
    get() = TODO()
  val f32: Float
    get() = TODO()
  val f64: Double
    get() = TODO()

  fun putInto(stack: Stack)
}

interface GlobalVarMutable : GlobalVar {
  override var s32: Int
    get() = TODO()
    set(value) = TODO()
  override var u32: UInt
    get() = TODO()
    set(value) = TODO()
  override var u64: ULong
    get() = TODO()
    set(value) = TODO()
  override var s64: Long
    get() = TODO()
    set(value) = TODO()
  override var f32: Float
    get() = TODO()
    set(value) = TODO()
  override var f64: Double
    get() = TODO()
    set(value) = TODO()

  fun setFrom(stack: Stack)

  class S32(value: Int = 0) : GlobalVarMutable {
    override var s32: Int = value

    override fun putInto(stack: Stack) {
      stack.pushI32(s32)
    }

    override fun setFrom(stack: Stack) {
      s32 = stack.popI32()
    }
  }

  class S64(value: Long = 0L) : GlobalVarMutable {
    override var s64: Long = value

    override fun putInto(stack: Stack) {
      stack.pushI64(s64)
    }

    override fun setFrom(stack: Stack) {
      s64 = stack.popI64()
    }
  }


}
