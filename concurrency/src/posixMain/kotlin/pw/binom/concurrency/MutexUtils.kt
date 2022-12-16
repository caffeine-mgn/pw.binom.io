package pw.binom.concurrency

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import platform.posix.pthread_cond_t
import platform.posix.pthread_mutex_t
import platform.posix.timeval
import platform.posix.timezone

expect fun internalPthread_mutex_init(mutex: CPointer<pthread_mutex_t>, attr: COpaquePointer?): Int
expect fun internalPthread_cond_init(mutex: CPointer<pthread_cond_t>, attr: COpaquePointer?): Int
expect fun internalGettimeofday(tv: CPointer<timeval>, tz: CPointer<timezone>?): Int
