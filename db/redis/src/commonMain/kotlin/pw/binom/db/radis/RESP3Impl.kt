package pw.binom.db.radis

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.charset.Charset
import pw.binom.charset.Charsets
import pw.binom.collections.defaultMutableList
import pw.binom.collections.defaultMutableMap
import pw.binom.collections.defaultMutableSet
import pw.binom.io.*

class RESP3Impl(
  val output: AsyncOutput,
  val input: AsyncInput,
  val closeParent: Boolean,
  charset: Charset = Charsets.UTF8,
  bufferSize: Int = DEFAULT_BUFFER_SIZE,
) : RESP {
  private val reader =
    AsyncBufferedReaderInput(
      stream = input,
      closeParent = false,
      charset = charset,
      bufferSize = bufferSize,
    )
  private val writer = output.bufferedWriter(closeParent = false, charset = charset)
  private val outBuffer = ByteArrayOutput()
//    private val tmp = ByteBuffer(2)
//    private val decoder = charset.newDecoder()
//    private suspend fun AsyncBufferedInput.readChar(): Char? {
//        tmp.reset(0, 1)
//        val readed = this.readFully(tmp)
//        if (readed == 0) {
//            return null
//        }
//        if (readed != 1) {
//            throw RadisException("Invalid read length $readed")
//        }
//        tmp.clear()
//        return tmp[0].toInt().toChar()
//    }

//    private suspend fun AsyncBufferedInput.readln(): String {
//        while (true) {
//            tmp.clear()
//            tmp.limit = 1
//            if (this.read(tmp) != 1) {
//                break
//            }
//            tmp.flip()
//            if (tmp[0] == 10.toByte()) {
//                break
//            }
//            outBuffer.write(tmp)
//        }
//        outBuffer.locked { data ->
//            data.limit--
//            return CharBuffer.alloc(data.remaining).use { d ->
//                decoder.decode(data, d)
//                d.flip()
//                d.toString()
//            }
//        }
//    }

//    private suspend fun AsyncBufferedInput.readString(size: Int): String {
//        outBuffer.clear()
//        outBuffer.alloc(size)
//        val data = outBuffer.lock()
//        data.reset(0, size)
//        this.readFully(data)
//        data.flip()
//        return CharBuffer.alloc(data.remaining).use { d ->
//            decoder.decode(data, d)
//            outBuffer.clear()
//            d.flip()
//            d.toString()
//        }
//    }

//    private suspend fun AsyncBufferedInput.skipCLRL() {
//        tmp.clear()
//        this.readFully(tmp)
//    }

  override suspend fun flush() {
    writer.flush()
    output.flush()
  }

  override suspend fun asyncClose() {
    outBuffer.clear()
    outBuffer.close()
    reader.asyncClose()
    writer.asyncClose()
//        tmp.close()
    if (closeParent) {
      input.asyncClose()
      output.asyncClose()
    }
  }

  private suspend fun internalReadInlineString(): String = reader.readln()

  private suspend fun internalReadStringOrNull(): String? {
    val line = reader.readln()
    val len = line.toIntOrNull() ?: throw IllegalStateException("Invalid String Bulk length \"$line\"")
    if (len < 0) {
      return null
    }
    val str = reader.readString(len)
    reader.skipCRLF()
    return str
  }

  private suspend fun internalReadStringDataOrNull(func: suspend (ByteBuffer) -> Unit): Boolean {
    val line = reader.readln()
    val len = line.toIntOrNull() ?: throw IllegalStateException("Invalid String Bulk length \"$line\"")
    if (len < 0) {
      return false
    }
    outBuffer.clear()
    outBuffer.alloc(len)
    val d = outBuffer.lock()
    d.clear()
    d.limit = len
    input.readFully(d)
    func(d)
    outBuffer.clear()
    return true
  }

  private suspend fun internalReadBoolean(): Boolean =
    when (val len = reader.readln()) {
      "t" -> true
      "f" -> false
      else -> throw IllegalStateException("Invalid Boolean \"$len\"")
    }

  private suspend fun internalReadLong(): Long? {
    val num = reader.readln()
    if (num == "_") {
      return null
    }
    return num.toLongOrNull() ?: throw IllegalStateException("Invalid Integer \"$num\"")
  }

  private suspend fun internalReadDouble(): Double? =
    when (val num = reader.readln()) {
      "_" -> null
      "inf" -> Double.POSITIVE_INFINITY
      "-inf" -> Double.NEGATIVE_INFINITY
      else -> num.toDoubleOrNull() ?: throw IllegalStateException("Invalid Decimal \"$num\"")
    }

  private suspend fun internalReadList(): List<Any?>? {
    val line = reader.readln()
    val len = line.toIntOrNull() ?: throw IllegalStateException("Invalid List length \"$line\"")
    if (len < 0) { // TODO обработать бесконечный список
      return null
    }
    val result = defaultMutableList<Any?>(len)
    repeat(len) {
      result += readResponse()
    }
    return result
  }

  private suspend fun internalReadSet(): Set<Any?>? {
    val line = reader.readln()
    val len = line.toIntOrNull() ?: throw IllegalStateException("Invalid Set length \"$line\"")
    if (len < 0) { // TODO обработать бесконечный список
      return null
    }
    val result = defaultMutableSet<Any?>()
    repeat(len) {
      result += readResponse()
    }
    return result
  }

  private suspend fun internalReadMap(): Map<Any?, Any?>? {
    val line = reader.readln()
    val len = line.toIntOrNull() ?: throw IllegalStateException("Invalid Map size \"$line\"")
    if (len < 0) { // TODO обработать бесконечный список
      return null
    }
    val result = defaultMutableMap<Any?, Any?>()
    repeat(len) {
      val key = readResponse()
      val value = readResponse()
      result[key] = value
    }
    return result
  }

  suspend fun readString(): String? {
    val char = readFirstChar()
    return when (char) {
      '+' -> internalReadInlineString()
      '$' -> internalReadStringOrNull()
      else -> throw RadisException("Expected \"$\" but actual \"$char\"")
    }
  }

  suspend fun readStringDataByteArray(): ByteArray? {
    var b: ByteArray? = null
    readStringDataByteBuffer { b = it.toByteArray() }
    return b
  }

  suspend fun readStringDataByteBuffer(func: suspend (ByteBuffer) -> Unit): Boolean =
    when (val char = readFirstChar()) {
      '+' -> {
        outBuffer.clear()
        outBuffer.write(internalReadInlineString().encodeToByteArray())
        func(outBuffer.lock())
        outBuffer.clear()
        true
      }
      '$' -> internalReadStringDataOrNull(func)
      else -> throw RadisException("Expected \"$\" but actual \"$char\"")
    }

//    suspend fun getStringAsByteBuffer(key: String, func: suspend (ByteBuffer) -> Unit): Boolean {
//        val size = readResposeStringBytes(outBuffer) ?: return false
//        if (size == 0) {
//            return true
//        }
//        val buf = outBuffer.lock()
//        buf.limit = buf.position + size
//        func(buf)
//        outBuffer.clear()
//        return true
//    }

  suspend fun readLong(): Long? =
    when (val char = readFirstChar()) {
      ':' -> internalReadLong()
      else -> throw RadisException("Expected \":\" but actual \"$char\"")
    }

  suspend fun readDouble(): Double? =
    when (val char = readFirstChar()) {
      ':' -> internalReadDouble()
      else -> throw RadisException("Expected \":\" but actual \"$char\"")
    }

  suspend fun readList(): List<Any?>? =
    when (val char = readFirstChar()) {
      '*' -> internalReadList()
      else -> throw RadisException("Expected \"*\" but actual \"$char\"")
    }

  suspend fun readSet(): Set<Any?>? =
    when (val char = readFirstChar()) {
      '~' -> internalReadSet()
      else -> throw RadisException("Expected \"~\" but actual \"$char\"")
    }

  suspend fun readMap(): Map<Any?, Any?>? =
    when (val char = readFirstChar()) {
      '%' -> internalReadMap()
      else -> throw RadisException("Expected \"%\" but actual \"$char\"")
    }

  private suspend fun readFirstChar(): Char {
    val char = reader.readANSIChar() ?: throw RadisException("Data EOF")
    if (char == '-') {
      val err = reader.readln()
      when {
        err.startsWith("ERR ") -> throw RedisCommonException(err.removePrefix("ERR "))
        else -> throw RadisException(reader.readln())
      }
    }
    return char
  }

  suspend fun readResponse(): Any? =
    when (val char = readFirstChar()) {
      '+' -> internalReadInlineString()
      '$' -> internalReadStringOrNull()
      '#' -> internalReadBoolean()
      ':' -> internalReadLong()
      ',' -> internalReadDouble()
      '*' -> internalReadList()
      '~' -> internalReadSet()
      '%' -> internalReadMap()
      else -> throw RadisException("Unknown message type \"$char\"")
    }

  suspend fun writeSimpleString(str: String) {
    val findCLRL =
      str.any {
        val code = it.code
        code == 10 || code == 13 || code == 32
      }
    if (findCLRL) {
      throw IllegalArgumentException("Input string \"$str\" contains invalid chars")
    }
    writer.append("+").append(str).append("\r\n")
  }

  suspend fun writeASCIStringFast(str: String) {
    writer.append("$").append(str.length).append("\r\n").append(str).append("\r\n")
  }

  suspend fun writeDataString(data: ByteArray) {
    outBuffer.clear()
    outBuffer.write(data)
    writeDataString(outBuffer.lock())
    outBuffer.clear()
  }

  suspend fun writeDataString(data: ByteBuffer) {
    writer.append("$").append(data.remaining).append("\r\n")
    writer.flush()
    output.writeFully(data)
    output.flush()
    writer.append("\r\n")
  }

  suspend fun writeString(value: String) {
    outBuffer.clear()
    outBuffer.write(value.encodeToByteArray())
    writeDataString(outBuffer.lock())
  }

  suspend fun writeNull() {
    writer.append("$-1\r\n")
  }

  suspend fun startList(size: Int) {
    writer.append("*").append(size.toString()).append("\r\n")
  }

  suspend fun startSet(length: Int) {
    writer.append("~").append(length.toString()).append("\r\n")
  }

  suspend fun startMap(length: Int) {
    writer.append("%").append(length.toString()).append("\r\n")
  }

  suspend fun writeList(value: List<Any?>) {
    startList(value.size)
    value.forEach {
      writeValue(it)
    }
  }

  suspend fun writeBoolean(value: Boolean) {
    val str =
      if (value) {
        "t"
      } else {
        "f"
      }
    writer.append("#").append(str).append("\r\n")
  }

  private suspend fun writeInteger(value: String) {
    writer.append(":").append(value).append("\r\n")
  }

  private suspend fun writeDouble(value: String) {
    writer.append(",").append(value).append("\r\n")
  }

  suspend fun writeDouble(value: Double) = writeDouble(value.toString())

  suspend fun writeDouble(value: Float) = writeDouble(value.toString())

  suspend fun writeLong(value: Byte) = writeInteger(value.toString())

  suspend fun writeLong(value: Short) = writeInteger(value.toString())

  suspend fun writeLong(value: Int) = writeInteger(value.toString())

  suspend fun writeLong(value: Long) = writeInteger(value.toString())

  suspend fun writeLong(value: UByte) = writeInteger(value.toString())

  suspend fun writeLong(value: UShort) = writeInteger(value.toString())

  suspend fun writeLong(value: UInt) = writeInteger(value.toString())

  suspend fun writeLong(value: ULong) = writeInteger(value.toString())

  suspend fun writeSet(value: Set<Any?>) {
    startSet(value.size)
    value.forEach {
      writeValue(it)
    }
  }

  suspend fun writeMap(value: Map<Any?, Any?>) {
    startMap(value.size)
    value.forEach {
      writeValue(it.key)
      writeValue(it.value)
    }
  }

  private suspend fun writeValue(value: Any?) {
    when (value) {
      null -> writeNull()
      is String -> {
        writeString(value)
      }
      is Boolean -> {
        writeBoolean(value)
      }
      is Long, is Int, is Short, is Byte,
      is ULong, is UInt, is UShort, is UByte,
      -> {
        writeInteger(value.toString())
      }
      is Float, is Double -> {
        writeDouble(value.toString())
      }
      is List<*> -> {
        writeList(value)
      }
      is Set<*> -> {
        writeSet(value)
      }
      is Map<*, *> -> {
        writeMap(value as Map<Any?, Any?>)
      }
    }
  }
}
