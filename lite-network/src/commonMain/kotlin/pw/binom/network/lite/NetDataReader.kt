package pw.binom.network.lite

import pw.binom.fromBytes
import pw.binom.io.Buffer
import pw.binom.io.UTF8
import pw.binom.io.socket.NetworkAddress

public open class NetDataReader {
    @PublishedApi
    internal var _data: ByteArray? = null

    @PublishedApi
    internal var _position = 0

    @PublishedApi
    internal var _dataSize = 0

    @PublishedApi
    internal var _offset = 0

    public inline val RawData: ByteArray?
        get() = _data

    public inline val RawDataSize
        get() = _dataSize

    public inline val UserDataOffset
        get() = _offset

    public val UserDataSize
        get() = _dataSize - _offset

    public inline val IsNull
        get() = _data == null

    public inline val Position
        get() = _position

    public inline val EndOfData
        get() = _position == _dataSize

    public inline val AvailableBytes
        get() = _dataSize - _position

    public fun SkipBytes(count: Int) {
        _position += count
    }

    public fun SetPosition(position: Int) {
        _position = position
    }

    public fun SetSource(dataWriter: NetDataWriter) {
        _data = dataWriter.Data
        _position = 0
        _offset = 0
        _dataSize = dataWriter.Length
    }

    public fun SetSource(source: ByteArray) {
        _data = source
        _position = 0
        _offset = 0
        _dataSize = source.size
    }

    public fun SetSource(source: ByteArray, offset: Int, maxSize: Int) {
        _data = source
        _position = offset
        _offset = offset
        _dataSize = maxSize
    }

    public constructor() {
    }

    public constructor(writer: NetDataWriter) {
        SetSource(writer)
    }

    public constructor(source: ByteArray) {
        SetSource(source)
    }

    public constructor(source: ByteArray, offset: Int, maxSize: Int) {
        SetSource(source, offset, maxSize)
    }

    public fun GetNetEndPoint(): NetworkAddress {
        val host = GetString(1000) ?: TODO()
        val port = GetInt()
        return NetworkAddress.create(host = host, port = port)
//        return NetUtils.MakeEndPoint(host, port);
    }

    public fun GetByte(): Byte {
        val res = _data!![_position]
        _position++
        return res
    }

    public fun GetSByte(): Byte {
        return GetByte()
    }

    private fun readArraySize(): Int {
        val result = Short.fromBytes(_data!!, _position).toInt()
        _position += Short.SIZE_BYTES
        return result
    }

    private fun readByte(): Byte {
        val result = _data!![_position]
        _position++
        return result
    }

    private fun readBool() = readByte() == 1.toByte()

    private fun readShort(): Short {
        val result = Short.fromBytes(_data!!, _position)
        _position += Short.SIZE_BYTES
        return result
    }

    private fun readInt(): Int {
        val result = Int.fromBytes(_data!!, _position)
        _position += Int.SIZE_BYTES
        return result
    }

    private fun readLong(): Long {
        val result = Long.fromBytes(_data!!, _position)
        _position += Long.SIZE_BYTES
        return result
    }

    private fun readFloat() = Float.fromBits(readInt())
    private fun readDouble() = Double.fromBits(readLong())

    public fun GetBoolArray() = BooleanArray(readArraySize()) { readBool() }

    public fun GetUShortArray() = UShortArray(readArraySize()) { readShort().toUShort() }

    public fun GetShortArray() = ShortArray(readArraySize()) { readShort() }

    public fun GetIntArray() = IntArray(readArraySize()) { readInt() }

    public fun GetUIntArray() = UIntArray(readArraySize()) { readInt().toUInt() }

    public fun GetFloatArray() = FloatArray(readArraySize()) { readFloat() }

    public fun GetDoubleArray() = DoubleArray(readArraySize()) { readDouble() }

    public fun GetLongArray() = LongArray(readArraySize()) { readLong() }

    public fun GetULongArray() = ULongArray(readArraySize()) { readLong().toULong() }

    public fun GetStringArray(): Array<String?> {
        val length = GetUShort().toInt()
        return Array(length) { GetString() }
    }

    // / <summary>
    // / Note that "maxStringLength" only limits the number of characters in a string, not its size in bytes.
    // / Strings that exceed this parameter are returned as empty
    // / </summary>
    public fun GetStringArray(maxStringLength: Int): Array<String?> {
        val length = GetUShort().toInt()
        return Array(length) { GetString(maxStringLength) }
    }

    public fun GetBool(): Boolean {
        return GetByte() == 1.toByte()
    }

    public fun GetChar() = GetUShort().asChar

    public fun GetUShort(): UShort {
        val result = BitConverter.ToUInt16(_data!!, _position)
        _position += 2
        return result
    }

    public fun GetShort(): Short {
        val result = BitConverter.ToInt16(_data!!, _position)
        _position += 2
        return result
    }

    public fun GetLong() = readLong()

    public fun GetULong() = readLong().toULong()

    public fun GetInt() = readInt()

    public fun GetUInt() = readInt().toUInt()

    public fun GetFloat() = readFloat()

    public fun GetDouble() = readDouble()

    // / <summary>
    // / Note that "maxLength" only limits the number of characters in a string, not its size in bytes.
    // / </summary>
    // / <returns>"string.Empty" if value > "maxLength"</returns>
    public fun GetString(maxLength: Int): String? {
        val size = GetUShort().toInt()
        if (size == 0) {
            return null
        }

        val actualSize = size - 1
        if (actualSize >= NetDataWriter.StringBufferMaxLength) {
            return null
        }

        val data = GetBytesSegment(actualSize)

        return if (maxLength > 0 && UTF8.getUtfCharCount(data) > maxLength) {
            ""
        } else {
            data.decodeToString()
        }
    }

    public fun GetString(): String? {
        val size = GetUShort().toInt()
        if (size == 0) {
            return null
        }

        val actualSize = size - 1
        if (actualSize >= NetDataWriter.StringBufferMaxLength) {
            return null
        }

        val data = GetBytesSegment(actualSize)
        return data.decodeToString()
//        return NetDataWriter.uTF8Encoding.GetString(data.Array, data.Offset, data.Count);
    }

    public fun GetBytesSegment(count: Int) = ByteArray(count) { readByte() } // TODO оптимизировать копирование памяти

    public fun GetRemainingBytesSegment() = GetBytesSegment(AvailableBytes)

    public fun GetRemainingBytes(): ByteArray {
        val outgoingData = ByteArray(AvailableBytes)
        Buffer.BlockCopy(_data!!, _position, outgoingData, 0, AvailableBytes)
        _position = _data!!.size
        return outgoingData
    }

    public fun GetBytes(destination: ByteArray, start: Int, count: Int) {
        Buffer.BlockCopy(_data!!, _position, destination, start, count)
        _position += count
    }

    public fun GetBytes(destination: ByteArray, count: Int) {
        Buffer.BlockCopy(_data!!, _position, destination, 0, count)
        _position += count
    }

    public fun GetSBytesWithLength(): ByteArray = ByteArray(readArraySize()) { readByte() }

    public fun GetBytesWithLength() = ByteArray(readArraySize()) { readByte() }

    public fun PeekByte() = _data!![_position]

    public fun PeekSByte() = _data!![_position]

    public fun PeekBool() = _data!![_position] == 1.toByte()

    public fun PeekChar() = PeekUShort().asChar

    public fun PeekUShort(): UShort = BitConverter.ToUInt16(_data!!, _position)

    public fun PeekShort() = BitConverter.ToInt16(_data!!, _position)

    public fun PeekLong() = BitConverter.ToInt64(_data!!, _position)

    public fun PeekULong() = BitConverter.ToUInt64(_data!!, _position)

    public fun PeekInt() = BitConverter.ToInt32(_data!!, _position)

    public fun PeekUInt() = BitConverter.ToUInt32(_data!!, _position)

    public fun PeekFloat() = BitConverter.ToSingle(_data!!, _position)

    public fun PeekDouble() = BitConverter.ToDouble(_data!!, _position)

    // / <summary>
    // / Note that "maxLength" only limits the number of characters in a string, not its size in bytes.
    // / </summary>
    public fun PeekString(maxLength: Int): String? {
        val size = PeekUShort().toInt()
        if (size == 0) {
            return null
        }

        val actualSize = size - 1
        if (actualSize >= NetDataWriter.StringBufferMaxLength) {
            return null
        }

        return if (maxLength > 0 && NetDataWriter.uTF8Encoding.GetCharCount(
                _data,
                _position + 2,
                actualSize,
            ) > maxLength
        ) {
            ""
        } else {
            NetDataWriter.uTF8Encoding.GetString(_data, _position + 2, actualSize)
        }
    }

    public fun PeekString(): String? {
        val size = PeekUShort().toInt()
        if (size == 0) {
            return null
        }

        val actualSize = size - 1
        if (actualSize >= NetDataWriter.StringBufferMaxLength) {
            return null
        }

        return NetDataWriter.uTF8Encoding.GetString(_data, _position + 2, actualSize)
    }

    public fun TryGetByte(result: (Byte) -> Unit): Boolean {
        if (AvailableBytes >= 1) {
            result(GetByte())
            return true
        }
        result(0)
        return false
    }

    public fun TryGetSByte(result: (Byte) -> Unit): Boolean {
        if (AvailableBytes >= 1) {
            result(GetSByte())
            return true
        }
        result(0)
        return false
    }

    public fun TryGetBool(result: (Boolean) -> Unit): Boolean {
        if (AvailableBytes >= 1) {
            result(GetBool())
            return true
        }
        result(false)
        return false
    }

    public fun TryGetChar(result: (Char) -> Unit): Boolean {
        var uShortValue = 0.toUShort()
        if (!TryGetUShort { uShortValue = it }) {
            result(0.toChar())
            return false
        }
        result(uShortValue.asChar)
        return true
    }

    public fun TryGetShort(result: (Short) -> Unit): Boolean {
        if (AvailableBytes >= 2) {
            result(GetShort())
            return true
        }
        result(0)
        return false
    }

    public fun TryGetUShort(result: (UShort) -> Unit): Boolean {
        if (AvailableBytes >= 2) {
            result(GetUShort())
            return true
        }
        result(0.toUShort())
        return false
    }

    public fun TryGetInt(result: (Int) -> Unit): Boolean {
        if (AvailableBytes >= 4) {
            result(GetInt())
            return true
        }
        result(0)
        return false
    }

    public fun TryGetUInt(result: (UInt) -> Unit): Boolean {
        if (AvailableBytes >= 4) {
            result(GetUInt())
            return true
        }
        result(0u)
        return false
    }

    public fun TryGetLong(result: (Long) -> Unit): Boolean {
        if (AvailableBytes >= 8) {
            result(GetLong())
            return true
        }
        result(0)
        return false
    }

    public fun TryGetULong(result: (ULong) -> Unit): Boolean {
        if (AvailableBytes >= 8) {
            result(GetULong())
            return true
        }
        result(0.toULong())
        return false
    }

    public fun TryGetFloat(result: (Float) -> Unit): Boolean {
        if (AvailableBytes >= 4) {
            result(GetFloat())
            return true
        }
        result(0f)
        return false
    }

    public fun TryGetDouble(result: (Double) -> Unit): Boolean {
        if (AvailableBytes >= 8) {
            result(GetDouble())
            return true
        }
        result(0.0)
        return false
    }

    public fun TryGetString(result: (String?) -> Unit): Boolean {
        if (AvailableBytes >= 2) {
            val strSize = PeekUShort().toInt()
            if (AvailableBytes >= strSize + 1) {
                result(GetString())
                return true
            }
        }
        result(null)
        return false
    }

    public fun TryGetStringArray(result: (Array<String?>?) -> Unit): Boolean {
        var strArrayLength: UShort = 0.toUShort()
        if (!TryGetUShort { strArrayLength = it }) {
            result(null)
            return false
        }

        val list = arrayOfNulls<String?>(strArrayLength.toInt())
        for (i in 0..strArrayLength.toInt()) {
            var str: String? = null
            if (!TryGetString { str = it }) {
                result(null)
                return false
            }
        }
        result(list)

        return true
    }

    public fun TryGetBytesWithLength(result: (ByteArray?) -> Unit): Boolean {
        if (AvailableBytes >= 2) {
            val length = PeekUShort().toInt()
            if (length >= 0 && AvailableBytes >= 2 + length) {
                result(GetBytesWithLength())
                return true
            }
        }
        result(null)
        return false
    }

    public fun Clear() {
        _position = 0
        _dataSize = 0
        _data = null
    }

//    public inline fun <reified T : Any> GetArray(size: Int): Array<T> {
//        var length = BitConverter.ToUInt16(_data!!, _position).toInt()
//        _position += 2;
//        val result = Array<T?>(length) { null }
//        length = length * size;
//        Buffer.BlockCopy(_data, _position, result, 0, length);
//        _position += length;
//        return result;
//    }
//
//    public T Get<T>() where T : INetSerializable, new()
//    {
//        var obj = new T ();
//        obj.Deserialize(this);
//        return obj;
//    }
}
