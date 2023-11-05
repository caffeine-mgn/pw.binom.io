package pw.binom.concurrency

import kotlinx.cinterop.*
import platform.posix.*

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
actual fun internalPthread_mutex_init(mutex: CPointer<pthread_mutex_t>, attr: COpaquePointer?) =
  pthread_mutex_init(mutex, attr?.reinterpret())

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
actual fun internalPthread_cond_init(mutex: CPointer<pthread_cond_t>, attr: COpaquePointer?): Int =
  pthread_cond_init(mutex, attr?.reinterpret())

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@OptIn(ExperimentalForeignApi::class)
actual fun internalGettimeofday(tv: CPointer<timeval>, tz: CPointer<timezone>?): Int =
  gettimeofday(tv, tz)
