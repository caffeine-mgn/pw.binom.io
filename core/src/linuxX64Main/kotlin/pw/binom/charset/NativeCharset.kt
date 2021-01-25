package pw.binom.charset

import pw.binom.isBigEndian

//actual val NATIVE_CHARSET="UTF-32LE
actual val NATIVE_CHARSET = if (pw.binom.Environment.isBigEndian) "UCS-2BE" else "UCS-2LE"