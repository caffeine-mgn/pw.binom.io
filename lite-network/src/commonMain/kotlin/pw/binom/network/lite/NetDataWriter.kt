package pw.binom.network.lite

import pw.binom.io.Buffer
import pw.binom.io.socket.NetworkAddress

public class NetDataWriter
{
    companion object{
        private const val InitialSize = 64;
        val uTF8Encoding = UTF8Encoding(false, true);
        public const val StringBufferMaxLength = 1024 * 32; // <- short.MaxValue + 1

        /// <summary>
        /// Creates NetDataWriter from existing ByteArray
        /// </summary>
        /// <param name="bytes">Source byte array</param>
        /// <param name="copy">Copy array to new location or use existing</param>
        public fun FromBytes(bytes:ByteArray, copy:Boolean):NetDataWriter
        {
            if (copy)
            {
                var netDataWriter = NetDataWriter(true, bytes.size);
                netDataWriter.Put(bytes);
                return netDataWriter;
            }
            return NetDataWriter(true, 0).apply {
                _data = bytes
                _position = bytes.size
            }
        }

        /// <summary>
        /// Creates NetDataWriter from existing ByteArray (always copied data)
        /// </summary>
        /// <param name="bytes">Source byte array</param>
        /// <param name="offset">Offset of array</param>
        /// <param name="length">Length of array</param>
        public fun FromBytes(bytes:ByteArray, offset:Int, length:Int):NetDataWriter
        {
            var netDataWriter = NetDataWriter(true, bytes.size)
            netDataWriter.Put(bytes, offset, length)
            return netDataWriter
        }

        public fun FromString(value:String):NetDataWriter
        {
            val netDataWriter = NetDataWriter();
            netDataWriter.Put(value);
            return netDataWriter;
        }
    }
    @PublishedApi
    internal var _data:ByteArray
    @PublishedApi
    internal var _position=0

    private val _autoResize:Boolean

    public inline val Capacity
        get()=_data.size

    public val Data
        get()=_data


    public inline val Length
        get()=_position

    // Cache encoding instead of creating it with BinaryWriter each time
    // 1000 readers before: 1MB GC, 30ms
    // 1000 readers after: .8MB GC, 18ms

    private val _stringBuffer = ByteArray(StringBufferMaxLength)

    public constructor() : this(true, InitialSize)
    {
    }

    public constructor(autoResize:Boolean) : this(autoResize, InitialSize)
    {
    }

    public constructor(autoResize:Boolean, initialSize:Int)
    {
        _data = ByteArray(initialSize);
        _autoResize = autoResize;
    }



    public inline fun ResizeIfNeed(newSize:Int)
    {
        if (_data.size < newSize)
        {
            _data = _data.copyOf(maxOf(newSize, _data.size * 2))
        }
    }

    public inline fun EnsureFit(additionalSize:Int)
    {
        if (_data.size < _position + additionalSize)
        {
            _data=_data.copyOf(maxOf(_position + additionalSize, _data.size * 2))

        }
    }

    public fun Reset(size:Int)
    {
        ResizeIfNeed(size);
        _position = 0;
    }

    public fun Reset()
    {
        _position = 0;
    }

    public fun CopyData():ByteArray
    {
        val resultData = ByteArray(_position)
        Buffer.BlockCopy(_data, 0, resultData, 0, _position);
        return resultData;
    }

    /// <summary>
    /// Sets position of NetDataWriter to rewrite previous values
    /// </summary>
    /// <param name="position">new byte position</param>
    /// <returns>previous position of data writer</returns>
    public fun SetPosition(position:Int):Int
    {
        val prevPosition = _position;
        _position = position;
        return prevPosition;
    }

    public fun Put(value:Float)
    {
        if (_autoResize)
            ResizeIfNeed(_position + 4);
        FastBitConverter.GetBytes(_data, _position, value);
        _position += 4;
    }

    public fun Put(value:Double)
    {
        if (_autoResize)
            ResizeIfNeed(_position + 8);
        FastBitConverter.GetBytes(_data, _position, value);
        _position += 8;
    }

    public fun Put(value:Long)
    {
        if (_autoResize)
            ResizeIfNeed(_position + 8);
        FastBitConverter.GetBytes(_data, _position, value);
        _position += 8;
    }

    public fun Put(value:ULong)
    {
        if (_autoResize)
            ResizeIfNeed(_position + 8);
        FastBitConverter.GetBytes(_data, _position, value);
        _position += 8;
    }

    public fun Put(value:Int)
    {
        if (_autoResize)
            ResizeIfNeed(_position + 4);
        FastBitConverter.GetBytes(_data, _position, value);
        _position += 4;
    }

    public fun Put(value:UInt)
    {
        if (_autoResize)
            ResizeIfNeed(_position + 4);
        FastBitConverter.GetBytes(_data, _position, value);
        _position += 4;
    }

    public fun Put(value:Char)
    {
        Put(value.asUShort)
    }

    public fun Put(value:UShort)
    {
        if (_autoResize)
            ResizeIfNeed(_position + 2);
        FastBitConverter.GetBytes(_data, _position, value);
        _position += 2;
    }

    public fun Put(value:Short)
    {
        if (_autoResize)
            ResizeIfNeed(_position + 2);
        FastBitConverter.GetBytes(_data, _position, value);
        _position += 2;
    }

    public fun Put(value:Byte)
    {
        if (_autoResize)
            ResizeIfNeed(_position + 1);
        _data[_position] = value;
        _position++;
    }

    public fun Put(value:UByte)
    {
        if (_autoResize)
            ResizeIfNeed(_position + 1);
        _data[_position] = value.toByte();
        _position++;
    }

    public fun Put(data:ByteArray, offset:Int, length:Int)
    {
        if (_autoResize)
            ResizeIfNeed(_position + length);
        Buffer.BlockCopy(data, offset, _data, _position, length);
        _position += length;
    }

    public fun Put(data:ByteArray)
    {
        if (_autoResize)
            ResizeIfNeed(_position + data.size);
        Buffer.BlockCopy(data, 0, _data, _position, data.size);
        _position += data.size;
    }

    public fun PutSBytesWithLength(data:ByteArray, offset:Int, length:UShort)
    {
        if (_autoResize)
            ResizeIfNeed(_position + 2 + length);
        FastBitConverter.GetBytes(_data, _position, length);
        Buffer.BlockCopy(data, offset, _data, _position + 2, length.toInt());
        _position += 2 + length.toInt();
    }

    public fun PutSBytesWithLength(data:ByteArray)
    {
        PutArray(data, 1);
    }

    public void PutBytesWithLength(byte[] data, int offset, ushort length)
    {
        if (_autoResize)
            ResizeIfNeed(_position + 2 + length);
        FastBitConverter.GetBytes(_data, _position, length);
        Buffer.BlockCopy(data, offset, _data, _position + 2, length);
        _position += 2 + length;
    }

    public fun PutBytesWithLength(data:ByteArray)
    {
        PutArray(data, 1);
    }

    public fun Put(value:Boolean)
    {
        Put((byte)(value ? 1 : 0));
    }

    public void PutArray(Array arr, int sz)
    {
        ushort length = arr == null ? (ushort) 0 : (ushort)arr.Length;
        sz *= length;
        if (_autoResize)
            ResizeIfNeed(_position + sz + 2);
        FastBitConverter.GetBytes(_data, _position, length);
        if (arr != null)
            Buffer.BlockCopy(arr, 0, _data, _position + 2, sz);
        _position += sz + 2;
    }

    public fun PutArray(value:FloatArray)
    {
        PutArray(value, 4);
    }

    public fun PutArray(value:DoubleArray)
    {
        PutArray(value, 8);
    }

    public fun PutArray(value:LongArray)
    {
        PutArray(value, 8);
    }

    public fun PutArray(value:ULongArray)
    {
        PutArray(value, 8);
    }

    public fun PutArray(value:IntArray)
    {
        PutArray(value, 4);
    }

    public fun PutArray(value:UIntArray)
    {
        PutArray(value, 4);
    }

    public fun PutArray(value:UShortArray)
    {
        PutArray(value, 2);
    }

    public fun PutArray(value:ShortArray)
    {
        PutArray(value, 2);
    }

    public fun PutArray(value:BooleanArray)
    {
        PutArray(value, 1);
    }

    public fun PutArray(value:Array<String>)
    {
        ushort strArrayLength = value == null ? (ushort)0 : (ushort)value.Length;
        Put(strArrayLength);
        for (int i = 0; i < strArrayLength; i++)
        Put(value[i]);
    }

    public fun PutArray(value:Array<String>, strMaxLength:Int)
    {
        ushort strArrayLength = value == null ? (ushort)0 : (ushort)value.Length;
        Put(strArrayLength);
        for (int i = 0; i < strArrayLength; i++)
        Put(value[i], strMaxLength);
    }

    public fun Put(endPoint:NetworkAddress)
    {
        Put(endPoint.host);
        Put(endPoint.port);
    }

    public fun Put(value:String)
    {
        Put(value, 0);
    }

    /// <summary>
    /// Note that "maxLength" only limits the number of characters in a string, not its size in bytes.
    /// </summary>
    public fun Put(value:String?, maxLength:Int)
    {
        if (value == null)
        {
            Put(0.toUShort())
            return
        }

        val length = if (maxLength > 0 && value.length > maxLength ) maxLength else value.length;
        val buffer = value.encodeToByteArray()
        val size = buffer.size

        if (size >= StringBufferMaxLength)
        {
            Put(0.toUShort());
            return;
        }

        Put(checked((ushort)(size + 1)));
        Put(buffer, 0, size);
    }

    public void Put<T>(T obj) where T : INetSerializable
    {
        obj.Serialize(this);
    }
}
