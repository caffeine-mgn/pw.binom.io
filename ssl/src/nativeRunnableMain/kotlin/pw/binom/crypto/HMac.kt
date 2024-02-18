package pw.binom.crypto

import kotlinx.cinterop.*
import platform.openssl.*
import pw.binom.io.ByteBuffer
import pw.binom.security.MessageDigest
import pw.binom.security.NoSuchAlgorithmException

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
actual class HMac actual constructor(val algorithm: AlgorithmType, val key: ByteArray) : MessageDigest {
  private var ctx: CPointer<HMAC_CTX>? = null

  actual enum class AlgorithmType(val code: String, val size: Int, val make: () -> AlgorithmInstance) {
    SHA512(code = "HmacSHA512", 64, { AlgorithmInstance.sha512() }),
    SHA256(code = "HmacSHA256", 32, { AlgorithmInstance.sha256() }),
    SHA384(code = "HmacSHA384", 32, { AlgorithmInstance.sha384() }),
    SHA1(code = "HmacSHA1", 20, { AlgorithmInstance.sha1() }),
    MD5(code = "HmacMD5", 16, { AlgorithmInstance.md5() }),
    ;

    actual companion object {
      private val content = HashMap<String, AlgorithmType>()

      init {
        values().forEach {
          content[it.name.lowercase()] = it
          content[it.code.lowercase()] = it
        }
      }

      actual fun find(name: String): AlgorithmType? = content[name.lowercase()]

      /**
       * @throws NoSuchAlgorithmException throws when algorithm [name] not found
       */
      actual fun get(name: String): AlgorithmType = find(name = name) ?: throw NoSuchAlgorithmException(name)
    }
  }

  private fun checkInit() {
    if (this.ctx != null) {
      return
    }
    val ctx = HMAC_CTX_new()!!
    key.usePinned { keyPinned ->
      HMAC_Init_ex(ctx, keyPinned.addressOf(0), keyPinned.get().size, algorithm.make().ptr, null)
    }
    this.ctx = ctx
  }

  override fun init() {
    if (ctx != null) {
      HMAC_CTX_free(ctx)
      ctx = null
    }
    checkInit()
  }

  override fun update(byte: Byte) {
    update(ByteArray(1) { byte })
  }

  override fun update(input: ByteArray, offset: Int, len: Int) {
    checkInit()
    memScoped {
      HMAC_Update(ctx, input.refTo(offset).getPointer(this).reinterpret(), len.convert())
    }
  }

  override fun update(buffer: ByteBuffer) {
    checkInit()
    memScoped {
      buffer.ref(0) { bufferPtr, remaining ->
        HMAC_Update(ctx, bufferPtr.getPointer(this).reinterpret(), remaining.convert())
      }
    }
  }

  override fun finish(): ByteArray {
    checkInit()
    val out = ByteArray(algorithm.size)
    out.usePinned { outPinned ->
      memScoped {
        val size = alloc<UIntVar>()
        HMAC_Final(ctx, outPinned.addressOf(0).getPointer(this).reinterpret(), size.ptr)
        if (size.value != out.size.convert<UInt>()) {
          TODO()
        }
        HMAC_CTX_free(ctx)
        ctx = null
      }
    }
    return out
  }
}
