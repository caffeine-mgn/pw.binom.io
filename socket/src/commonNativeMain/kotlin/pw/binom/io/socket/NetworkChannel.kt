package pw.binom.io.socket

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

internal const val RAW_SOCKET_CLIENT_TYPE = 0x001b
internal const val RAW_SOCKET_SERVER_TYPE = 0x010b

actual interface NetworkChannel : Channel {
    val nsocket: RawSocket
    val type: Int
}

@OptIn(ExperimentalContracts::class)
internal inline fun isClient(self:NetworkChannel):Boolean{
        contract {
            returns(true) implies (self is RawSocketChannel)
        }
        return (self.type and RAW_SOCKET_CLIENT_TYPE) > 0
    }

@OptIn(ExperimentalContracts::class)
internal inline fun isServer(self:NetworkChannel):Boolean{
    contract {
        returns(true) implies (self is RawServerSocketChannel)
    }
    return (self.type and RAW_SOCKET_SERVER_TYPE) > 0
}