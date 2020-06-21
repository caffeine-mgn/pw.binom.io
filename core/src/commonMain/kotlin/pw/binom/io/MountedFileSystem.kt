package pw.binom.io

import pw.binom.AsyncOutput
import pw.binom.AsyncInput

class MountedFileSystem<U> : FileSystem<U> {

    inner class MountPoint<T>(path: String, val newFileSystem: FileSystem<T>, val func: (U) -> T) : FileSystem.Entity<U> {
        override val name: String
            get() = TODO("Not yet implemented")
        override val length: Long
            get() = TODO("Not yet implemented")
        override val isFile: Boolean
            get() = false
        override val lastModified: Long
            get() = 0
        override val path: String
            get() = TODO("Not yet implemented")
        override val user: U
            get() = TODO("Not yet implemented")
        override val fileSystem: FileSystem<U>
            get() = this@MountedFileSystem

        override suspend fun read(): AsyncInput? {
            TODO("Not yet implemented")
        }

        override suspend fun copy(path: String, overwrite: Boolean): FileSystem.Entity<U> {
            TODO("Not yet implemented")
        }

        override suspend fun move(path: String, overwrite: Boolean): FileSystem.Entity<U> {
            TODO("Not yet implemented")
        }

        override suspend fun delete() {
            TODO("Not yet implemented")
        }

        override suspend fun rewrite(): AsyncOutput {
            TODO("Not yet implemented")
        }
    }

    private val mounts = HashMap<String, MountedFileSystem<Any>>()

    fun <T> mount(path: String, fileSystem: FileSystem<T>, func: (U) -> T): MountPoint<T> {
        if (mounts.containsKey(path))
            TODO()
        val mp = MountPoint(
                path = path,
                newFileSystem = fileSystem,
                func = func
        )
        mounts[path] = mp as MountedFileSystem<Any>
        return mp
    }

    override suspend fun mkdir(user: U, path: String): FileSystem.Entity<U>? {
        TODO("Not yet implemented")
    }

    override suspend fun getDir(user: U, path: String): Sequence<FileSystem.Entity<U>>? {
        TODO("Not yet implemented")
    }

    override suspend fun get(user: U, path: String): FileSystem.Entity<U>? {
        TODO("Not yet implemented")
    }

    override suspend fun new(user: U, path: String): AsyncOutput {
        TODO("Not yet implemented")
    }

}