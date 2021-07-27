package pw.binom.io.file

import pw.binom.*
import pw.binom.io.FileSystem
import pw.binom.io.use
import pw.binom.io.withLimit
import pw.binom.pool.ObjectPool

class LocalFileSystem(
    val root: File,
    val byteBufferPool: ObjectPool<ByteBuffer>
) : FileSystem {
    override suspend fun new(path: String): AsyncOutput {
        val file = File(root, path.removePrefix("/"))
        file.parent?.mkdirs()
        return file.write().asyncOutput()
    }

    override suspend fun get(path: String): FileSystem.Entity? {
        val f = File(root, path.removePrefix("/"))
        if (!f.isExist)
            return null
        return EntityImpl(f)
    }

    override val isSupportUserSystem: Boolean
        get() = false

    override suspend fun <T> useUser(user: Any?, func: suspend () -> T): T =
        func()

    override suspend fun mkdir(path: String): FileSystem.Entity {
        val file = File(root, path.removePrefix("/"))
        file.mkdirs()
        return EntityImpl(file)
    }


    override suspend fun getDir(path: String): Sequence<FileSystem.Entity>? {
        if (path == "/")
            return root.listEntities()

        val f = File(root, path.removePrefix("/"))
        if (f.isDirectory)
            return f.listEntities()
        return null
    }

    private suspend fun File.listEntities(): Sequence<EntityImpl> {
        val out = ArrayList<EntityImpl>()
        this.iterator().forEach {
            out += EntityImpl(it)
        }


        return out.asSequence()
    }

    private inner class EntityImpl(val file: File) : FileSystem.Entity {
        override val fileSystem: FileSystem
            get() = this@LocalFileSystem

        override suspend fun read(offset: ULong, length: ULong?): AsyncInput? {
            val file = File(root, path.removePrefix("/"))
            if (!file.isFile)
                return null
            val channel = file.read()
            if (offset > 0uL) {
                channel.position = offset
            }

            return length?.let { channel.asyncInput().withLimit(it.toLong()) } ?: channel.asyncInput()
        }

        override suspend fun copy(path: String, overwrite: Boolean): FileSystem.Entity {
            val toFile = File(root, path.removePrefix("/"))

            if (toFile.isExist && !overwrite)
                throw FileSystem.EntityExistException(path)

            if (!file.isExist)
                throw FileSystem.FileNotFoundException(this.path)

            this.file.read().use { s ->
                toFile.write().use { d ->
                    s.copyTo(d, byteBufferPool)
                }
            }
            return EntityImpl(toFile)
        }

        override suspend fun move(path: String, overwrite: Boolean): FileSystem.Entity {
            val toFile = File(root, path.removePrefix("/"))

            if (toFile.isExist && !overwrite)
                throw FileSystem.EntityExistException(path)

            if (!file.isExist)
                throw FileSystem.FileNotFoundException(this.path)

            file.renameTo(toFile)
            return EntityImpl(toFile)
        }

        override suspend fun delete() {
            File(root, path.removePrefix("/")).deleteRecursive()
        }

        override suspend fun rewrite(): AsyncOutput {
            val file = File(root, path)
            file.parent?.mkdirs()
            return file.write().asyncOutput()
        }

        override val path: String
            get() = file.path.removePrefix(root.path).replace('\\', '/')
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