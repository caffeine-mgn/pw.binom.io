package pw.binom.mq.nats.client

import pw.binom.collections.emptyIterator

class BytesParsedHeaders() : MutableParsedHeaders {
  private var dataByteSize = 0
  private val values = HashMap<String, Item>()

  constructor(map: Map<String, List<String>>) : this() {
    map.forEach { (key, values) ->
      values.forEach { value ->
        add(key, value)
      }
    }
  }

  private class Item(val keyBytes: ByteArray) {
    val values = ArrayList<ByteArray>()

    fun write(
      out: ByteArray,
      cursor: Int,
      setCursor: (Int) -> Unit,
    ) {
      var newCursor = cursor
      val keySize = keyBytes.size
      values.forEach { value ->
        keyBytes.copyInto(
          destination = out,
          destinationOffset = newCursor,
        )
        newCursor += keySize
        out[newCursor++] = DOUBLE_DOT
        out[newCursor++] = SPACE
        value.copyInto(
          destination = out,
          destinationOffset = newCursor,
        )
        newCursor += value.size
        out[newCursor++] = CL
        out[newCursor++] = RF
      }
      setCursor(newCursor)
    }

    operator fun contains(second: String): Boolean =
      values.any {
        second == it.decodeToString()
      }
  }

  companion object {
    internal val HEADER_VERSION = "NATS/1.0\r\n".encodeToByteArray()

    /**
     * `:`
     */
    internal val DOUBLE_DOT = ':'.code.toByte()
    internal val CL = 0x0D.toByte() // \r
    internal val RF = 0x0A.toByte() // \n
    internal val SPACE = ' '.code.toByte() // \n
  }

  override fun add(
    key: String,
    value: String,
  ) {
    val exist = values[key]
    if (exist != null) {
      val valueData = value.encodeToByteArray()
      exist.values += valueData
      dataByteSize += exist.keyBytes.size + valueData.size
    } else {
      val keyBytes = key.encodeToByteArray()
      val newItem =
        Item(
          keyBytes = keyBytes,
        )
      val valueData = value.encodeToByteArray()
      newItem.values += valueData
      dataByteSize += keyBytes.size + valueData.size
      values[key] = newItem
    }
  }

  override fun get(key: String): List<String>? = values[key]?.values?.map { it.decodeToString() }

  override fun clone(): BytesParsedHeaders {
    val l = BytesParsedHeaders()
    l.values.putAll(values)
    return l
  }

  override val isEmpty: Boolean
    get() = values.isEmpty()

  override fun toHeadersBody(): HeadersBody {
    if (isEmpty) {
      return HeadersBody.empty
    }
    var rowCount = 0
    values.forEach {
      rowCount += it.value.values.size
    }
    val size = dataByteSize + rowCount * 2 + rowCount * 2
    val out = ByteArray(size + HEADER_VERSION.size)
    HEADER_VERSION.copyInto(out)
    var cursor = HEADER_VERSION.size
    values.forEach {
      it.value.write(out, cursor, { cursor = it })
    }
    return HeadersBody(out)
  }

  override val size: Int
    get() {
      var sum = 0
      values.forEach {
        sum += it.value.values.size
      }
      return sum
    }

  private class MapIterator(map: Map<String, Item>) : Iterator<Pair<String, String>> {
    private val mapIterator = map.iterator()
    private var listIterator: Iterator<ByteArray> = emptyIterator()

    override fun hasNext(): Boolean = listIterator.hasNext() || mapIterator.hasNext()

    private var currentKey = ""

    override fun next(): Pair<String, String> {
      if (listIterator.hasNext()) {
        return currentKey to listIterator.next().decodeToString()
      }
      if (!mapIterator.hasNext()) {
        throw NoSuchElementException()
      }
      val e = mapIterator.next()
      currentKey = e.key
      listIterator = e.value.values.iterator()
      return currentKey to listIterator.next().decodeToString()
    }
  }

  override fun iterator(): Iterator<Pair<String, String>> = MapIterator(values)
}
