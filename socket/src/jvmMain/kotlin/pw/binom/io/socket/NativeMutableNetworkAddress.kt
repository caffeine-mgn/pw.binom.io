package pw.binom.io.socket

import java.net.*
//
//class NativeMutableNetworkAddress : MutableNetworkAddress {
//    private var _native: InetSocketAddress? = null
//
//    internal fun overrideNative(native: InetSocketAddress) {
//        type = if (native.address is Inet4Address)
//            NetworkAddress.Type.IPV4
//        else
//            NetworkAddress.Type.IPV6
//        host = native.address.hostAddress
//        port = native.port
//        this._native = native
//    }
//
//    val native: InetSocketAddress
//        get() {
//            if (_native == null) {
//                val address = when (type) {
//                    NetworkAddress.Type.IPV4 -> Inet4Address.getByName(host)
//                    NetworkAddress.Type.IPV6 -> Inet6Address.getByName(host)
//                }
//                _native = InetSocketAddress(address, port)
//            }
//            return _native!!
//        }
//
//    override var host: String = ""
//        set(value) {
//            field = value
//            _native = null
//        }
//    override var port: Int = 0
//        set(value) {
//            field = value
//            _native = null
//        }
//    override var type: NetworkAddress.Type = NetworkAddress.Type.IPV4
//        set(value) {
//            field = value
//            _native = null
//        }
//
//    override fun set(address: NetworkAddress) {
//        type = address.type
//        host = address.host
//        port = address.port
//    }
//
//    override fun reset(type: NetworkAddress.Type, host: String, port: Int) {
//        val address = when (type) {
//            NetworkAddress.Type.IPV4 -> Inet4Address.getByName(host)
//            NetworkAddress.Type.IPV6 -> Inet6Address.getByName(host)
//        }
//        _native = InetSocketAddress(address, port)
//    }
//
//    override fun reset(host: String, port: Int) {
//        _native = InetSocketAddress(InetAddress.getByName(host), port)
//    }
//}
//