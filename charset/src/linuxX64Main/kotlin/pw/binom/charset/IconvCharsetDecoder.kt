package pw.binom.charset

import pw.binom.CharBuffer
import pw.binom.io.ByteBuffer
import pw.binom.isBigEndian

// val NATIVE_CHARSET = if (pw.binom.Environment.isBigEndian) "UCS-2BE" else "UCS-2LE"
// const val NATIVE_CHARSET = "WCHAR_T"
val NATIVE_CHARSET =
    if (Platform.osFamily == OsFamily.WINDOWS) "WCHAR_T" else if (pw.binom.Environment.isBigEndian) "UTF-16BE" else "UTF-16LE"

class IconvCharsetDecoder(name: String, onClose: ((AbstractIconv) -> Unit)?) : CharsetDecoder,
    AbstractIconv(fromCharset = name, toCharset = NATIVE_CHARSET, onClose = onClose) {

    init {
        CharsetMetrics.incDecoder()
    }

    override fun decode(input: ByteBuffer, output: CharBuffer): CharsetTransformResult =
        iconv(
            input,
            output
        )

    override fun close() {
        super.close()
        CharsetMetrics.decDecoder()
    }
}
