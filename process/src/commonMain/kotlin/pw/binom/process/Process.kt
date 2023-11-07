package pw.binom.process

import pw.binom.io.Closeable
import pw.binom.io.Input
import pw.binom.io.Output

interface Process : Closeable {
  companion object

  val pid: Long
  val stdin: Output
  val stdout: Input
  val stderr: Input

  /**
   * Returns process exit statis
   *
   * @throws ProcessStillActive throw when process still running
   */
  val exitStatus: Int
  val isActive: Boolean

  fun join()

  class ProcessStillActive : ProcessException()
}
