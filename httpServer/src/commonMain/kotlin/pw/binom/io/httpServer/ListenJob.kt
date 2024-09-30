package pw.binom.io.httpServer

import kotlinx.coroutines.Job

class ListenJob(val job: Job, val port: Int) : Job by job
