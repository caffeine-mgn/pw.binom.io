package pw.binom.coroutines

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job

class JobWithCancelWaiter(val job: Job, val func: (cause: CancellationException?) -> Unit) : Job by job {
  override fun cancel(cause: CancellationException?) {
    try {
      func(cause)
    } finally {
      job.cancel(cause)
    }
  }
}

fun Job.onCancel(func: (cause: CancellationException?) -> Unit) = JobWithCancelWaiter(
  job = this,
  func = func,
)
