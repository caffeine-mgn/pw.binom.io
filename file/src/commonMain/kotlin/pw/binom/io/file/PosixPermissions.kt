package pw.binom.io.file

import kotlin.jvm.JvmInline

@JvmInline
value class PosixPermissions(val mode: UInt) {
    companion object {
        const val OTHERS_EXECUTE = 1u // 1
        const val OTHERS_WRITE = 2u // 10
        const val OTHERS_READ = 4u // 100
        const val GROUP_EXECUTE = 8u // 1000
        const val GROUP_WRITE = 16u // 10000
        const val GROUP_READ = 32u // 100000
        const val OWNER_EXECUTE = 64u // 1000000
        const val OWNER_WRITE = 128u // 10000000
        const val OWNER_READ = 256u // 100000000
        const val STICKY_BIT = 512u // 1000000000
        const val GROUP_EXECUTE_PERMISSION = 1024u // 10000000000
        const val USER_EXECUTE_PERMISSION = 2048u // 100000000000
        const val DIRECTORY = 4096u // 1000000000000

        /**
         * -rwsrwsrwt
         */
        fun parse(permissions: String): PosixPermissions? {
            var state = 0
            fun test(char: Char, index: Int, value: UInt) = when (val c = permissions[index]) {
                char -> value
                '-' -> 0u
                else -> throw IllegalArgumentException("Invalid char \"$c\" in $index in \"$permissions\"")
            }

            fun r(index: Int, value: UInt) = test('r', index, value)
            fun w(index: Int, value: UInt) = test('w', index, value)
            val executeOther = when (val char = permissions[9]) {
                'x', 't' -> OTHERS_EXECUTE
                '-', 'T' -> 0u
                else -> return null
            }
            val writeOther = w(8, OTHERS_WRITE)
            val readOther = r(7, OTHERS_READ)
            val executeGroup = when (permissions[6]) {
                'x', 's' -> GROUP_EXECUTE
                '-', 'S' -> 0u
                else -> return null
            }
            val writeGroup = w(5, GROUP_WRITE)
            val readGroup = r(4, GROUP_READ)
            val executeOwner = when (permissions[3]) {
                'x', 's' -> OWNER_EXECUTE
                '-', 'S' -> 0u
                else -> return null
            }
            val writeOwner = w(2, OWNER_WRITE)
            val readOwner = r(1, OWNER_READ)
            val stickyBit = when (val char = permissions[9]) {
                't', 'T' -> STICKY_BIT
                'x', '-' -> 0u
                else -> return null
            }
            val executeGroupPermission = when (permissions[6]) {
                'S', 's' -> GROUP_EXECUTE_PERMISSION
                '-', 'x' -> 0u
                else -> return null
            }
            val executeUserPermission = when (permissions[3]) {
                'S', 's' -> USER_EXECUTE_PERMISSION
                '-', 'x' -> 0u
                else -> return null
            }
            val directory = when (permissions[0]) {
                '-' -> 0u
                'd' -> DIRECTORY
                else -> return null
            }
            val intValue = readOwner or writeOwner or executeOwner or readGroup or
                writeGroup or executeGroup or readOther or writeOther or executeOther or
                stickyBit or executeGroupPermission or executeUserPermission or directory
            return PosixPermissions(intValue)
        }
    }

    fun toStringOctal() = mode.toString(8)
    override fun toString(): String {
        val mode = mode
        val sb = StringBuilder(9)

        sb.append(if (mode and DIRECTORY > 0u) 'd' else '-')

        sb.append(if (mode and OWNER_READ > 0u) 'r' else '-')
        sb.append(if (mode and OWNER_WRITE > 0u) 'w' else '-')
        sb.append(
            when {
                mode and OWNER_EXECUTE > 0u && mode and USER_EXECUTE_PERMISSION > 0u -> 's'
                mode and USER_EXECUTE_PERMISSION > 0u -> 'S'
                mode and OWNER_EXECUTE > 0u -> 'x'
                else -> '-'
            }
        )

        sb.append(if (mode and GROUP_READ > 0u) 'r' else '-')
        sb.append(if (mode and GROUP_WRITE > 0u) 'w' else '-')
        sb.append(
            when {
                mode and GROUP_EXECUTE > 0u && mode and GROUP_EXECUTE_PERMISSION > 0u -> 's'
                mode and GROUP_EXECUTE_PERMISSION > 0u -> 'S'
                mode and GROUP_EXECUTE > 0u -> 'x'
                else -> '-'
            }
        )

        sb.append(if (mode and OTHERS_READ > 0u) 'r' else '-')
        sb.append(if (mode and OTHERS_WRITE > 0u) 'w' else '-')
        sb.append(
            when {
                mode and OTHERS_EXECUTE > 0u && mode and STICKY_BIT > 0u -> 't'
                mode and STICKY_BIT > 0u -> 'T'
                mode and OTHERS_EXECUTE > 0u -> 'x'
                else -> '-'
            }
        )

        return sb.toString()
    }

    operator fun plus(mode: UInt) = PosixPermissions(this.mode or mode)
    operator fun minus(mode: UInt) = PosixPermissions((this.mode.inv() or mode.inv()).inv())
    operator fun contains(mode: UInt): Boolean = this.mode and mode > 0u
    operator fun contains(posixPermissions: PosixPermissions): Boolean = this.mode and posixPermissions.mode > 0u
}

operator fun UInt.contains(posixPermissions: PosixPermissions): Boolean = this and posixPermissions.mode > 0u
