package pw.binom.wasm.runner.cmd

import pw.binom.wasm.node.inst.Inst
import pw.binom.wasm.node.inst.Memory
import pw.binom.wasm.runner.*
import pw.binom.wasm.runner.stack.Stack

object MemoryRunner {
  fun run(cmd: Memory, stack: Stack, memory: List<MemorySpace>): Inst? {
    return when (cmd) {
      is Memory.F32_LOAD -> TODO()
      is Memory.F32_STORE -> TODO()
      is Memory.F64_LOAD -> TODO()
      is Memory.F64_STORE -> TODO()
      is Memory.I32_LOAD -> {
        val offset = stack.pop() as Value.Primitive.I32
        val mem = memory[cmd.memoryId]
        val address = cmd.offset + offset.value.toUInt()
        val intValue = mem.getI32(offset = address, align = cmd.align)
        val value = Value.Primitive.I32(intValue)
        stack.push(value)
        cmd.next
      }

      is Memory.I32_LOAD16_S -> TODO()
      is Memory.I32_LOAD16_U -> {
        val offset = stack.popI32()
        val mem = memory[cmd.memoryId]
        val address = cmd.offset + offset.toUInt()
        val value = mem.getI16(address,cmd.align).toInt()
        stack.pushI32(value)
        cmd.next
      }

      is Memory.I32_LOAD8_S -> {
        val offset = stack.popI32()
        val mem = memory[cmd.memoryId]
        val address = cmd.offset + offset.toUInt()
        val value = mem.getI8(address).toInt()
        stack.pushI32(value)
        cmd.next
      }

      is Memory.I32_LOAD8_U -> {
        val offset = stack.popI32()
        val mem = memory[cmd.memoryId]
        val address = cmd.offset + offset.toUInt()
        val value = mem.getI8(address).toInt()
        stack.pushI32(value)
        cmd.next
      }

      is Memory.I32_STORE -> {
        val value = stack.popI32()
        val address = stack.popI32()
        val mem = memory[cmd.memoryId]
        mem.pushI32(
          value = value,
          offset = address.toUInt() + cmd.offset,
          align = cmd.align,
        )
        cmd.next
      }

      is Memory.I32_STORE16 -> {
        val value = stack.popI32()
        val address = stack.popI32()
        val mem = memory[cmd.memoryId]
        mem.pushI16(
          value = value.toShort(),
          offset = address.toUInt() + cmd.offset,
          align = cmd.align,
        )
        cmd.next
      }

      is Memory.I32_STORE8 -> {
        val value = stack.popI32()
        val address = stack.popI32()
        val mem = memory[cmd.memoryId]
        mem.pushI8(
          value = value.toByte(),
          offset = address.toUInt() + cmd.offset,
          align = cmd.align,
        )
        cmd.next
      }

      is Memory.I64_LOAD -> {
        val offset = stack.popI32()
        val mem = memory[cmd.memoryId]
        stack.pushI64(
          mem.getI64(
            cmd.offset + offset.toUInt()
          )
        )
        cmd.next
      }

      is Memory.I64_LOAD16_S -> TODO()
      is Memory.I64_LOAD16_U -> TODO()
      is Memory.I64_LOAD32_S -> {
        val offset = stack.popI32()
        val mem = memory[cmd.memoryId]
        val address = cmd.offset + offset.toUInt()
        val value = mem.getI32(address,cmd.align).toLong()
        stack.pushI64(value)
        cmd.next
      }

      is Memory.I64_LOAD32_U -> {
        val offset = stack.popI32()
        val mem = memory[cmd.memoryId]
        val address = cmd.offset + offset.toUInt()
        val value = mem.getI32(address,cmd.align).toUInt().toLong()
        stack.pushI64(value)
        cmd.next
      }

      is Memory.I64_LOAD8_S -> TODO()
      is Memory.I64_LOAD8_U -> TODO()
      is Memory.I64_STORE -> {
        val value = stack.popI64()
        val address = stack.popI32()
        val mem = memory[cmd.memoryId]
        mem.pushI64(
          value = value,
          offset = address.toUInt() + cmd.offset,
          align = cmd.align,
        )
        cmd.next
      }

      is Memory.I64_STORE16 -> TODO()
      is Memory.I64_STORE32 -> TODO()
      is Memory.I64_STORE8 -> TODO()
    }
  }
}
