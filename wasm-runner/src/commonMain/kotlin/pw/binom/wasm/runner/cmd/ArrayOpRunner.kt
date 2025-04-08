package pw.binom.wasm.runner.cmd

import pw.binom.fromBytes
import pw.binom.reverse
import pw.binom.wasm.node.DataSection
import pw.binom.wasm.node.inst.ArrayOp
import pw.binom.wasm.node.inst.Inst
import pw.binom.wasm.runner.*
import pw.binom.wasm.runner.stack.Stack

object ArrayOpRunner {
  fun run(cmd: ArrayOp, stack: Stack, types: TypeDictionary, dataSection: DataSection): Inst? {
    return when (cmd) {
      is ArrayOp.Get -> {
        val index = stack.pop() as Value.Primitive.I32
        val struct = stack.pop() as Value.Ref.Array
        require(types.getRType(cmd.type) === struct.type)
        if (index.value >= struct.values.size) {
          TODO("Индекс элемента выходит за границы. Размер массива ${struct.values.size}. Запрошенный индекс ${index.value}")
        }
        stack.push(struct.values[index.value])
        cmd.next
      }

      is ArrayOp.GetS -> {
        val index = stack.popI32()
        val array = stack.pop() as Value.Ref.Array
        val value = array.values[index]
        require(value is Value.Primitive)
        stack.push(NumberUtils.getS(value))
        cmd.next
      }

      is ArrayOp.GetU -> {
        val index = stack.popI32()
        val array = stack.pop() as Value.Ref.Array
        val value = array.values[index]
        require(value is Value.Primitive)
        stack.push(NumberUtils.getU(value))
        cmd.next
      }

      is ArrayOp.NewByData -> {
        val arrayType = types.getRType(cmd.type) as RType.Ref.Array
        val p = arrayType.type as VType.Primitive
        val data = dataSection[cmd.data.id.toInt()].data
        val readFunc: (Int) -> Value = when (p.type) {
          RType.Primitive.F16 -> TODO()
          RType.Primitive.F32 -> TODO()
          RType.Primitive.F64 -> TODO()
          RType.Primitive.I16 -> { offset ->
            Value.Primitive.I16(Short.fromBytes(data, offset).reverse())
          }

          RType.Primitive.I32 -> { offset ->
            Value.Primitive.I32(Int.fromBytes(data, offset).reverse())
          }

          RType.Primitive.I64 -> TODO()
          RType.Primitive.I8 -> { offset ->
            Value.Primitive.I8(data[offset])
          }
        }
        val n = stack.pop() as Value.Primitive.I32
        val s = stack.pop() as Value.Primitive.I32
//            ValueHistory.self.print("n", n)
//            ValueHistory.self.print("s", s)
        check(data.size >= s.value + n.value * p.type.sizeInBytes) {
          "Not enough data length: ${data.size}, extends size ${(n.value + s.value) * p.type.sizeInBytes}"
        }
//            check(data.size >= (n.value + s.value) * p.type.sizeInBytes) {
//              "Not enough data length: ${data.size}, extends size ${(n.value + s.value) * p.type.sizeInBytes}"
//            }
        var offset = s.value// * p.type.sizeInBytes
        val result = ArrayList<Value>(n.value)
        repeat(n.value) {
          result += readFunc(offset)
          offset += p.type.sizeInBytes
        }
        stack.push(
          Value.Ref.Array(
            type = arrayType,
            values = result,
          )
        )
        cmd.next
      }

      is ArrayOp.NewByDefault -> {
        val size = stack.popI32()
        val arrType = types.getRType(cmd.type) as RType.Ref.Array
        val value = when (val tt = arrType.type) {
          is VType.Primitive -> {
            when (tt.type) {
              RType.Primitive.F16 -> TODO()
              RType.Primitive.F32 -> Value.Primitive.F32(0.0f)
              RType.Primitive.F64 -> Value.Primitive.F64(0.0)
              RType.Primitive.I16 -> Value.Primitive.I16(0)
              RType.Primitive.I32 -> {
                Value.Primitive.I32(0)
              }

              RType.Primitive.I64 -> Value.Primitive.I64(0)
              RType.Primitive.I8 -> Value.Primitive.I8(0)
            }
          }

          is VType.Ref -> {
            require(tt.nullable)
            Value.Ref.NULL
          }

          is VType.RefAbsolute -> {
            require(tt.nullable)
            Value.Ref.NULL
          }
        }
        val res = ArrayList<Value>(size)
        repeat(size) {
          res += value
        }
        stack.push(
          Value.Ref.Array(
            values = res,
            type = arrType,
          )
        )
        cmd.next
      }

      is ArrayOp.NewBySize -> {
        val r = ArrayList<Value>(cmd.size.toInt())
        repeat(cmd.size.toInt()) {
          r += stack.pop()
        }
        r.reverse()
//            for (i in (cmd.size.toInt()) downTo 0) {
//              r[i] = stack.pop()
//            }
        stack.push(Value.Ref.Array(type = types.getRType(cmd.type) as RType.Ref.Array, values = r))
        cmd.next
      }

      is ArrayOp.Set -> {
        val struct = types.getRType(cmd.type) as RType.Ref.Array
        val value = stack.pop()
        val index = stack.popI32()
        val array = stack.pop() as Value.Ref.Array
        require(array.type == struct)
        require(array.mutable) { "Array should be mutable" }
        array.values[index] = value
        cmd.next
      }
    }
  }
}
