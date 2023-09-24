package pw.binom.io.http

import pw.binom.io.AsyncWriter

interface HttpAsyncWriter : AsyncWriter {
  suspend fun getInput(): HttpInput?
}
