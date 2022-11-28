package pw.binom.io

import pw.binom.url.Path

class FileSystemWithDefaultUser(val user: Any, val fileSystem: FileSystem) : FileSystem {
    override suspend fun getQuota(path: Path): Quota? = fileSystem.useUser(user) {
        fileSystem.getQuota(path)
    }

    override val isSupportUserSystem: Boolean
        get() = false

    override suspend fun <T> useUser(user: Any?, func: suspend () -> T): T {
        throw UnsupportedOperationException()
    }

    override suspend fun mkdir(path: Path): FileSystem.Entity? =
        fileSystem.useUser(user) {
            fileSystem.mkdir(path)
        }

    override suspend fun getDir(path: Path): List<FileSystem.Entity>? =
        fileSystem.useUser(user) {
            fileSystem.getDir(path)
        }

    override suspend fun get(path: Path): FileSystem.Entity? =
        fileSystem.useUser(user) {
            fileSystem.get(path)
        }

    override suspend fun new(path: Path): AsyncOutput =
        fileSystem.useUser(user) {
            fileSystem.new(path)
        }

    override suspend fun new(path: Path, writeAction: suspend (AsyncOutput) -> Unit): FileSystem.Entity =
        fileSystem.useUser(user) {
            fileSystem.new(path, writeAction)
        }

    override suspend fun mkdirs(path: Path): FileSystem.Entity? =
        fileSystem.useUser(user) {
            fileSystem.mkdirs(path)
        }

    override fun toString(): String = "FileSystemWithDefaultUser(user: $user, filesystem: $fileSystem)"
}
