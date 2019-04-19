package pw.binom.io.zip

import platform.zlib.Z_BUF_ERROR
import platform.zlib.Z_DATA_ERROR
import platform.zlib.Z_OK
import platform.zlib.Z_STREAM_ERROR

fun zlibConsts(value: Int) =
        when (value) {
            Z_OK -> "Z_OK"
            Z_STREAM_ERROR -> "Z_STREAM_ERROR"
            Z_BUF_ERROR -> "Z_BUF_ERROR"
            Z_DATA_ERROR -> "Z_DATA_ERROR"
            else -> "Unknown ($value)"
        }