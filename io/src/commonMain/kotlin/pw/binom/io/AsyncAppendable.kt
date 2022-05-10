package pw.binom.io

interface AsyncAppendable {
    suspend fun append(value: CharSequence?): AsyncAppendable
    suspend fun append(value: Char): AsyncAppendable
    suspend fun append(value: Boolean): AsyncAppendable = append(value.toString())
    suspend fun append(value: Byte): AsyncAppendable = append(value.toString())
    suspend fun append(value: Short): AsyncAppendable = append(value.toString())
    suspend fun append(value: Int): AsyncAppendable = append(value.toString())
    suspend fun append(value: Long): AsyncAppendable = append(value.toString())
    suspend fun append(value: UByte): AsyncAppendable = append(value.toString())
    suspend fun append(value: UShort): AsyncAppendable = append(value.toString())
    suspend fun append(value: UInt): AsyncAppendable = append(value.toString())
    suspend fun append(value: ULong): AsyncAppendable = append(value.toString())
    suspend fun append(value: Float): AsyncAppendable = append(value.toString())
    suspend fun append(value: Double): AsyncAppendable = append(value.toString())
    suspend fun append(value: CharSequence?, startIndex: Int, endIndex: Int): AsyncAppendable
}
