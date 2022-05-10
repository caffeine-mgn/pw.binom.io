@file:JvmName("JvmUtils")

package pw.binom.network

import pw.binom.Environment
import kotlin.jvm.JvmName

expect val Short.hton: Short
expect val Short.ntoh: Short

expect val Int.hton: Int
expect val Int.ntoh: Int

expect val Long.hton: Long
expect val Long.ntoh: Long

expect val Environment.isBigEndian2: Boolean
