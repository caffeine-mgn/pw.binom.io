package pw.binom.io.socket

@WasmImport("wasi:sockets/udp-create-socket@0.2.5", "create-udp-socket")
internal external fun __wasm_import_createUdpSocket(p0: Int, p1: Int): Unit
