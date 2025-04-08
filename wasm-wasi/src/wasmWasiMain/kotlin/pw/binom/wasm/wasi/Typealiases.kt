package pw.binom.wasm.wasi

/**
 * Flags determining how to interpret the timestamp provided in
 * `subscription_clock::timeout`.
 */
typealias __wasi_subclockflags_t = UShort


/**
 * User-provided value that may be attached to objects that is retained when
 * extracted from the implementation.
 *
 * size 8
 * align 8
 */
typealias __wasi_userdata_t = ULong


/**
 * A file descriptor handle.
 *
 * size 4
 * align 4
 */
typealias __wasi_fd_t = Int

/**
 * Non-negative file size or length of a region within a file.
 * Size 8
 * Align 8
 */
typealias __wasi_filesize_t = ULong

/**
 * The state of the file descriptor subscribed to with
 * `eventtype::fd_read` or `eventtype::fd_write`.
 */
typealias __wasi_eventrwflags_t = UShort

/**
 *  Error codes returned by functions.
 *  Not all of these error codes are returned by the functions provided by this
 *  API; some are used in higher-level library layers, and others are provided
 *  merely for alignment with POSIX.
 */
typealias __wasi_errno_t = UShort

/**
 *  If set, treat the timestamp provided in
 *  `subscription_clock::timeout` as an absolute timestamp of clock
 *  `subscription_clock::id`. If clear, treat the timestamp
 *  provided in `subscription_clock::timeout` relative to the
 *  current time value of clock `subscription_clock::id`.
 */
val __WASI_SUBCLOCKFLAGS_SUBSCRIPTION_CLOCK_ABSTIME: UShort = (1 shl 0).toUShort()

/**
 * The clock measuring real time. Time value zero corresponds with
 * 1970-01-01T00:00:00Z.
 */
const val __WASI_CLOCKID_REALTIME = 0u

object EventType {
  const val __WASI_EVENTTYPE_CLOCK: UByte = 0u
  const val __WASI_EVENTTYPE_FD_READ: UByte = 1u
  const val __WASI_EVENTTYPE_FD_WRITE: UByte = 2u
}
typealias __wasi_eventtype_t = UByte
