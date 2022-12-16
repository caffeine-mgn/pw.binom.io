package pw.binom.network

import kotlinx.cinterop.convert
import platform.posix.htonl
import platform.posix.htons
import platform.posix.ntohl
import platform.posix.ntohs
import pw.binom.Environment

actual val Short.hton: Short
    get() = htons(this.convert()).convert()

actual val Short.ntoh: Short
    get() = ntohs(this.convert()).convert()

actual val Int.hton: Int
    get() = htonl(convert()).convert()

actual val Int.ntoh: Int
    get() = ntohl(convert()).convert()

actual val Long.hton: Long
    get() = this // internal_htonll(convert()).convert()

actual val Long.ntoh: Long
    get() = this // internal_ntohll(convert()).convert()

actual val Environment.isBigEndian2: Boolean
    get() = false // internal_isBigEndian() == 1.toUByte()
