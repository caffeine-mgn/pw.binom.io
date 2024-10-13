package pw.binom.wasm.runner

import pw.binom.Console

class WasiModule(val args: List<String>) : ImportResolver {
  companion object {
    const val __WASI_EBADF = 8
    const val ESUCCESS = 0
    const val __WASI_RIGHTS_FD_WRITE = 64
    const val __WASI_FILETYPE_CHARACTER_DEVICE = 2
  }

  var exitCode = 0
    private set

  override fun func(module: String, field: String): ((ExecuteContext) -> Unit)? {
    if (module != "wasi_snapshot_preview1") {
      return null
    }
    return when (field) {
      "args_get" -> { e ->
        val arrPtrPtr = e.args[0].asI32.value
        val bufferLenPtr = e.args[1].asI32.value
        var cursor = bufferLenPtr.toUInt()
        var cursor2 = arrPtrPtr
        args.forEach {
          val mem = it.encodeToByteArray()
          e.runner.memory[0].pushBytesWithZero(
            value = mem,
            offset = cursor
          )
          e.runner.memory[0].pushI32(
            value = cursor.toInt(),
            offset = cursor2.toUInt(),
            align = 0u,
          )
          cursor2 += Int.SIZE_BYTES
          cursor += (mem.size + 1).toUInt()
        }
//              arrPtrPtr
//              println()
        e.pushResult(Variable.I32(ESUCCESS))
//              TODO()
      }

      "args_sizes_get" -> { e ->
        val countPtr = e.args[0].asI32.value
        val bufferLenPtr = e.args[1].asI32.value
        val bufferLen = args.map { it.encodeToByteArray().size + 1 }.sum()
        e.runner.memory[0].pushI32(value = args.size, offset = countPtr.toUInt(), align = 0u)
        e.runner.memory[0].pushI32(value = bufferLen, offset = bufferLenPtr.toUInt(), align = 0u)
        e.pushResult(Variable.I32(ESUCCESS))
      }

      "proc_exit" -> { e ->
        exitCode = e.args[0].asI32.value
        e.stop()
//              println()
//              TODO()
      }

      "fd_close" -> { e ->
        println()
        TODO()
      }

      "fd_fdstat_get" -> FUNC@{ env ->
        val fd = env.args[0].asI32.value // reading __wasi_fd_t
        if (fd != 1 && fd != 2) {
          env.pushResult(Variable.I32(__WASI_EBADF))
          return@FUNC
        }
        val resultPtr = env.args[1].asI32.value
        // writing struct __wasi_fdstat_t
        env.runner.memory[0].pushI64(
          value = __WASI_FILETYPE_CHARACTER_DEVICE.toLong(),
          offset = (resultPtr + Long.SIZE_BYTES * 0).toUInt(),
          align = 0u
        )
        env.runner.memory[0].pushI64(
          value = __WASI_RIGHTS_FD_WRITE.toLong(),
          offset = (resultPtr + Long.SIZE_BYTES * 1).toUInt(),
          align = 0u
        )
        env.runner.memory[0].pushI64(
          value = 0,
          offset = (resultPtr + Long.SIZE_BYTES * 2).toUInt(),
          align = 0u
        )
        env.pushResult(Variable.I32(0))
      }

      "fd_seek" -> { e ->
        println()
      }

      "fd_write" -> { e ->
        // The file descriptor
        val fd = e.args[0].asI32.value
        if (fd != 1 && fd != 2) {
          e.pushResult(Variable.I32(__WASI_EBADF))
        } else {
          val channel = when (fd) {
            1 -> Console.std
            2 -> Console.err
            else -> TODO()
          }
//                Console.stdChannel.write()
          // The address of the scatter vector
          val iovs = e.args[1].asI32.value
          // The length of the scatter vector
          val iovsLen = e.args[2].asI32.value
          //The number of items written
          val nwritten = e.args[3].asI32.value
          val len = Int.SIZE_BYTES * 2
          var written = 0
          repeat(iovsLen) { count ->
            val buffer = e.runner.memory[0].getI32((iovs + count * len).toUInt()).toUInt()
            val bufferLen = e.runner.memory[0].getI32((iovs + count * len + Int.SIZE_BYTES).toUInt())
            val data = e.runner.memory[0].getBytes(
              offset = buffer,
              len = bufferLen,
            ).decodeToString()
            written += bufferLen
            channel.append(data)
          }
          e.runner.memory[0].pushI32(
            value = written,
            offset = nwritten.toUInt(),
            align = 0u,
          )
          e.pushResult(Variable.I32(ESUCCESS))
        }

      }

      else -> { e ->
        println()
        TODO("Unsupported call $module -> $field")
      }
//            else -> TODO("Unknown $field for module $module")
    }
    return super.func(module, field)
  }
}
