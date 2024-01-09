package pw.binom.strong.exceptions

import kotlin.coroutines.cancellation.CancellationException

class DestroyingException : CancellationException(null as String?)
