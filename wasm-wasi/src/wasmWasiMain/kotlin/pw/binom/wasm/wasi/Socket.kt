package pw.binom.wasm.wasi

// Similar to `AF_INET` in POSIX.
const val NETWORK_IP_ADDRESS_FAMILY_IPV4 = 0

// Similar to `AF_INET6` in POSIX.
const val NETWORK_IP_ADDRESS_FAMILY_IPV6 = 1

/**
 * @param address_family [NETWORK_IP_ADDRESS_FAMILY_IPV4] or [NETWORK_IP_ADDRESS_FAMILY_IPV6]
 * @param err tcp_create_socket_error_code_t*
 */
@WasmImport(module = "wasi:sockets/tcp-create-socket@0.2.0", name = "create-tcp-socket")
external fun __wasm_import_tcp_create_socket_create_tcp_socket(address_family: Int, ptr: UInt)

@WasmImport(module = "wasi_snapshot_preview1", name = "fd_renumber")
external fun __imported_wasi_snapshot_preview1_fd_renumber(
  fd: Int, // __wasi_fd_t
  to: Int, // __wasi_fd_t
): Int

@WasmImport(module = "wasi_snapshot_preview1", name = "sock_accept")
external fun __imported_wasi_snapshot_preview1_sock_accept(
  fd: Int, // __wasi_fd_t
  flags: Int, // __wasi_fdflags_t
  retptr0: Int,//__wasi_fd_t *
): Int // __wasi_errno_t

@WasmImport(module = "wasi_snapshot_preview1", name = "sock_recv")
external fun __imported_wasi_snapshot_preview1_sock_recv(
  fd: Int, // __wasi_fd_t
  ri_data: UInt, // const __wasi_iovec_t *
  ri_data_len: Int, // size_t
  ri_flags: Int, // __wasi_riflags_t
  retptr0: UInt, // __wasi_size_t *
  retptr1: UInt, // __wasi_roflags_t *
): Int // __wasi_errno_t

// Similar to `SHUT_RD` in POSIX.
const val TCP_SHUTDOWN_TYPE_RECEIVE = 0

// Similar to `SHUT_WR` in POSIX.
const val TCP_SHUTDOWN_TYPE_SEND = 1

// Similar to `SHUT_RDWR` in POSIX.
const val TCP_SHUTDOWN_TYPE_BOTH = 2

@WasmImport(module = "wasi_snapshot_preview1", name = "sock_shutdown")
external fun __imported_wasi_snapshot_preview1_sock_shutdown(
  fd: Int, // __wasi_fd_t
  how: Int, // __wasi_sdflags_t
): Int

@WasmImport(module = "wasi_snapshot_preview1", name = "sock_send")
external fun __imported_wasi_snapshot_preview1_sock_send(
  fd: Int, // __wasi_fd_t
  si_data: UInt, // onst __wasi_ciovec_t *
  si_data_len: Int, // size_t
  si_flags: Int, // __wasi_siflags_t
  retptr0: UInt, // __wasi_size_t *
): Int;
