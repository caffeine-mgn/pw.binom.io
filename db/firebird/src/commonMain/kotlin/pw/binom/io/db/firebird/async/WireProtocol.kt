package pw.binom.io.db.firebird.async

import pw.binom.Environment
import pw.binom.crypto.MD5MessageDigest
import pw.binom.crypto.crypt
import pw.binom.getEnv
import pw.binom.io.AsyncChannel
import pw.binom.io.ByteArrayOutput
import pw.binom.io.ByteBuffer
import pw.binom.io.writeByteArray
import pw.binom.writeByte
import pw.binom.writeInt

const val op_connect = 1
const val op_attach = 19
const val PLUGIN_LIST = "Srp256,Srp,Legacy_Auth"

class WireProtocol(val channel: AsyncChannel) {
    private val buffer = ByteBuffer(8)
    suspend fun packInt(value: Int) {
        channel.writeInt(value, buffer)
        channel.flush()
        println("Sent! $value")
    }

    suspend fun packString(str: String) {
        writeXDRBytes(str.encodeToByteArray())
    }

    suspend fun writeXDRBytes(bs: ByteArray) {
        // XDR encoding bytes
        val n = bs.size
        var padding = 0
        if (n % 4 != 0) {
            padding = 4 - n % 4
        }
        buffer.clear()
        buffer.writeInt(n)
        buffer.flip()
        channel.writeFully(buffer)
        channel.writeByteArray(bs, buffer)
        repeat(padding) { channel.writeByte(0, buffer) }
    }

    fun uid(
        uppercase: String,
        password: String,
        authPluginName: String,
        wireCrypt: Boolean,
        clientPublic: ByteArray?,
    ): ByteArray {
        val sysUser = Environment.getEnv("USER") ?: Environment.getEnv("USERNAME") ?: ""
        val hostname = "home"
        val sysUserBytes = sysUser.encodeToByteArray()
        val hostnameBytes = hostname.encodeToByteArray()
        val pluginListNameBytes = PLUGIN_LIST.encodeToByteArray()
        val pluginNameBytes = authPluginName.encodeToByteArray()
        val userBytes = uppercase.uppercase().encodeToByteArray()
        val wireCryptByte = if (wireCrypt) 1.toByte() else 0.toByte()

        val specific_data = when (authPluginName) {
            "Srp", "Srp256" -> {
//                getSrpClientPublicBytes(clientPublic)
                TODO()
            }

            "Legacy_Auth" -> {
                val md5 = MD5MessageDigest()
                md5.update(password.encodeToByteArray())
                md5.update("9z".encodeToByteArray())
                val b = md5.finish()
                println("->${b.toList()}")
                println(MD5MessageDigest.crypt(password = password, salt = "9z"))
                byteArrayOf(CNCT_specific_data, (b.size + 1).toByte()) + b + byteArrayOf(0)
            }

            else -> TODO()
        }
        val out = ByteArrayOutput()
        out.writeByte(CNCT_login)
        out.writeByte(userBytes.size.toByte())
        out.write(userBytes)

        out.writeByte(CNCT_plugin_name)
        out.writeByte(pluginNameBytes.size.toByte())
        out.write(pluginNameBytes)

        out.writeByte(CNCT_plugin_list)
        out.writeByte(pluginListNameBytes.size.toByte())
        out.write(pluginListNameBytes)

        out.write(specific_data)

        out.writeByte(CNCT_client_crypt)
        out.writeByte(4)
        out.writeByte(wireCryptByte)
        out.writeByte(0)
        out.writeByte(0)
        out.writeByte(0)

        out.writeByte(CNCT_user)
        out.writeByte(sysUserBytes.size.toByte())
        out.write(sysUserBytes)

        out.writeByte(CNCT_host)
        out.writeByte(hostnameBytes.size.toByte())
        out.write(hostnameBytes)

        out.writeByte(CNCT_user_verification)
        out.writeByte(0)
        return out.toByteArray()
    }
}

const val CNCT_login = 9.toByte()
const val CNCT_plugin_name = 8.toByte()
const val CNCT_plugin_list = 10.toByte()
const val CNCT_client_crypt = 11.toByte()
const val CNCT_user = 1.toByte()
const val CNCT_host = 4.toByte()
const val CNCT_specific_data = 7.toByte()
const val CNCT_user_verification = 6.toByte()
