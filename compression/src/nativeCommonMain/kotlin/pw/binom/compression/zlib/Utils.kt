package pw.binom.compression.zlib

import platform.zlib.*

internal fun zlibConsts(value: Int) =
    when (value) {
        Z_OK -> "Z_OK"
        Z_STREAM_ERROR -> "Z_STREAM_ERROR"
        Z_BUF_ERROR -> "Z_BUF_ERROR"
        Z_DATA_ERROR -> "Z_DATA_ERROR"
        Z_STREAM_END -> "Z_STREAM_END"
        else -> "Unknown ($value)"
    }