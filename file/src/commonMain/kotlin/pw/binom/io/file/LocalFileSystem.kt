package pw.binom.io.file

import pw.binom.ByteBufferPool
import pw.binom.asyncInput
import pw.binom.asyncOutput
import pw.binom.collections.defaultMutableList
import pw.binom.copyTo
import pw.binom.io.*
import pw.binom.url.Path
import pw.binom.url.toPath

class LocalFileSystem(
    val root: File,
    val byteBufferPool: ByteBufferPool
) : FileSystem {
    override suspend fun new(path: Path): AsyncOutput {
        val file = File(root, path.toString().removePrefix("/"))
        file.parent?.mkdirs() ?: throw FileSystem.FileNotFoundException((file.parent?.path ?: "").toPath)
        return file.openWrite().asyncOutput()
    }

    override suspend fun get(path: Path): FileSystem.Entity? {
        val f = File(root, path.toString().removePrefix("/"))
        if (!f.isExist) {
            return null
        }
        return EntityImpl(f)
    }

    override suspend fun getQuota(path: Path): Quota? {
        val f = File(root, path.toString())
        return Quota(
            availableBytes = f.freeSpace,
            usedBytes = f.freeSpace - f.availableSpace
        )
    }

    override val isSupportUserSystem: Boolean
        get() = false

    override suspend fun <T> useUser(user: Any?, func: suspend () -> T): T =
        func()

    override suspend fun mkdir(path: Path): FileSystem.Entity {
        val file = File(root, path.toString().removePrefix("/"))
        file.mkdirs() ?: throw FileSystem.FileNotFoundException(file.path.toPath)
        return EntityImpl(file)
    }

    override suspend fun getDir(path: Path): List<FileSystem.Entity>? {
        if (path.toString().isEmpty()) {
            return root.listEntities()
        }

        val f = File(root, path.toString().removePrefix("/"))
        if (f.isDirectory) {
            return f.listEntities()
        }
        return null
    }

    private suspend fun File.listEntities(): List<EntityImpl> {
        val out = defaultMutableList<EntityImpl>()
        this.iterator().forEach {
            out += EntityImpl(it)
        }
        return out
    }

    private inner class EntityImpl(val file: File) : FileSystem.Entity {
        override val fileSystem: FileSystem
            get() = this@LocalFileSystem

        override suspend fun read(offset: ULong, length: ULong?): AsyncInput? {
            val file = File(root, path.toString().removePrefix("/"))
            if (!file.isFile) {
                return null
            }
            val channel = file.openRead()
            if (offset > 0uL) {
                channel.position = offset.toLong()
            }
            var asyncChannel = channel.asyncInput()
            if (length != null) {
                asyncChannel = asyncChannel.withLimit(length.toLong())
            }
            return asyncChannel
        }

        override suspend fun copy(path: Path, overwrite: Boolean): FileSystem.Entity {
            val toFile = File(root, path.toString().removePrefix("/"))

            if (toFile.isExist && !overwrite) {
                throw FileSystem.EntityExistException(path)
            }

            if (!file.isExist) {
                throw FileSystem.FileNotFoundException(this.path)
            }

            this.file.openRead().use { source ->
                toFile.openWrite().use { destination ->
                    source.copyTo(destination, byteBufferPool)
                }
            }
            return EntityImpl(toFile)
        }

        override suspend fun move(path: Path, overwrite: Boolean): FileSystem.Entity {
            val toFile = File(root, path.toString().removePrefix("/"))

            if (toFile.isExist && !overwrite) {
                throw FileSystem.EntityExistException(path)
            }

            if (!file.isExist) {
                throw FileSystem.FileNotFoundException(this.path)
            }

            file.renameTo(toFile)
            return EntityImpl(toFile)
        }

        override suspend fun delete() {
            File(root, path.toString().removePrefix("/")).deleteRecursive()
        }

        override suspend fun rewrite(): AsyncOutput {
            val file = File(root, path.toString())
            file.parent?.mkdirs() ?: throw FileSystem.FileNotFoundException((file.parent?.path ?: "").toPath)
            return file.openWrite().asyncOutput()
        }

        override val path: Path
            get() = file.path.removePrefix(root.path).replace('\\', '/').toPath
        override val lastModified: Long
            get() = file.lastModified
        override val name: String
            get() = file.name
        override val length: Long
            get() = file.size
        override val isFile: Boolean
            get() = file.isFile
    }
}
