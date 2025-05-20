package pw.binom.strong

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

@OptIn(DelicateCoroutinesApi::class)
fun BeanLifeCycle.backgroundProcess(
  context: CoroutineContext? = null,
  func: suspend () -> Unit,
) {
  var job: Job? = null
  afterInit {
    job = GlobalScope.launch(context ?: coroutineContext) {
      func()
    }
  }
  preDestroy {
    job?.cancelAndJoin()
  }
}
