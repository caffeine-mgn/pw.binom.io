package pw.binom.concurrency

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.reinterpret
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
actual fun internalPthread_mutex_init(mutex: CPointer<pthread_mutex_t>, attr: COpaquePointer?) =
  pthread_mutex_init(mutex, attr?.reinterpret())

@OptIn(ExperimentalForeignApi::class)
actual fun internalPthread_cond_init(mutex: CPointer<pthread_cond_t>, attr: COpaquePointer?): Int =
  pthread_cond_init(mutex, attr?.reinterpret())

@OptIn(ExperimentalForeignApi::class)
actual fun internalGettimeofday(tv: CPointer<timeval>, tz: CPointer<timezone>?): Int =
  gettimeofday(tv, tz)
