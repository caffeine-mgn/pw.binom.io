package pw.binom.io.http

import pw.binom.io.AsyncOutput

interface HttpAsyncOutput: AsyncOutput {
  suspend fun getInput(): HttpInput?
}
