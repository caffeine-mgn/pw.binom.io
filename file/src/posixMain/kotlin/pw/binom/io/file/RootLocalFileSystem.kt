package pw.binom.io.file

import pw.binom.io.FileSystem2

actual object RootLocalFileSystem : FileSystem2 by LocalFileSystem2(File("/"))
