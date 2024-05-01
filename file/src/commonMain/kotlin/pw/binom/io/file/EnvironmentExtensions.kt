package pw.binom.io.file

import pw.binom.Environment
import pw.binom.workDirectory

val Environment.workDirectoryFile
  get() = File(Environment.workDirectory)
