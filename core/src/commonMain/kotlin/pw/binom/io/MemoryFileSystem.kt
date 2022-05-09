package pw.binom.io

import pw.binom.AsyncInput
import pw.binom.AsyncOutput
import pw.binom.asyncInput
import pw.binom.asyncOutput
import pw.binom.net.Path
import pw.binom.net.toPath

class MemoryFileSystem : FileSystem {
    override suspend fun getQuota(path: Path): Quota? = null

    override val isSupportUserSystem: Boolean
        get() = false

    private val root = FileEntity.Directory(name = "", parent = null)

    override suspend fun <T> useUser(user: Any?, func: suspend () -> T): T = func()

    override suspend fun mkdir(path: Path): FileSystem.Entity? {
        if (path.raw.isEmpty()) {
            return null
        }
        val it = path.elements.iterator()
        var current = root
        it.next()
        while (it.hasNext()) {
            val e = it.next()
            val found = current.entities.find { it.name == e }
                ?: return if (it.hasNext()) {
                    null
                } else {
                    val new = FileEntity.Directory(e, current)
                    current.entities += new
                    EntityWrapper(new)
                }
            if (found !is FileEntity.Directory) {
                return null
            }
            current = found
        }
        throw FileSystem.EntityExistException(path)
    }

    override suspend fun getDir(path: Path): List<FileSystem.Entity>? {
        if (path.raw.isEmpty()) {
            return root.entities.map { EntityWrapper(it) }
        }
        val it = path.elements.iterator()
        var current = root
        it.next()
        while (it.hasNext()) {
            val e = it.next()
            val found = current.entities.find { it.name == e } ?: return null
            if (found !is FileEntity.Directory) {
                return null
            }
            current = found
        }
        return current.entities.map { EntityWrapper(it) }
    }

    override suspend fun get(path: Path): FileSystem.Entity? {
        if (path.raw.isEmpty()) {
            return EntityWrapper(root)
        }
        val it = path.elements.iterator()
        var current = root
        it.next()
        while (it.hasNext()) {
            val e = it.next()
            val found = current.entities.find { it.name == e } ?: return null
            if (!it.hasNext()) {
                return EntityWrapper(found)
            }
            if (found !is FileEntity.Directory) {
                return null
            }
            current = found
        }
        return EntityWrapper(current)
    }

    override suspend fun new(path: Path): AsyncOutput {
        TODO("Not yet implemented")
    }

    private inner class EntityWrapper(val e: FileEntity) : FileSystem.Entity {
        override val length: Long
            get() = when (e) {
                is FileEntity.File -> e.body.size.toLong()
                is FileEntity.Directory -> 0
            }
        override val isFile: Boolean
            get() = e is FileEntity.File
        override val lastModified: Long
            get() = when (e) {
                is FileEntity.File -> e.lastModified
                is FileEntity.Directory -> 0
            }
        override val path: Path
            get() {
                val r = ArrayList<FileEntity>()
                var c = e
                do {
                    r += c
                    c = c.parent ?: break
                } while (true)
                r.reverse()
                return r.joinToString("/") { it.name }.toPath
            }
        override val fileSystem: FileSystem
            get() = this@MemoryFileSystem

        override suspend fun read(offset: ULong, length: ULong?): AsyncInput? =
            when (e) {
                is FileEntity.Directory -> null
                is FileEntity.File -> ByteArrayInput(e.body).asyncInput()
            }

        override suspend fun copy(path: Path, overwrite: Boolean): FileSystem.Entity {
            TODO("Not yet implemented")
        }

        override suspend fun move(path: Path, overwrite: Boolean): FileSystem.Entity {
            TODO("Not yet implemented")
        }

        override suspend fun delete() {
            if (e === root) {
                throw FileSystemAccess.AccessException.ForbiddenException()
            }
            val parent = e.parent ?: throw IllegalStateException("Maybe file ${e.name} already removed")
            if (!parent.entities.remove(e)) {
                throw IllegalStateException()
            }
        }

        override suspend fun rewrite(): AsyncOutput {
            val file = e as? FileEntity.File ?: throw FileSystemAccess.AccessException.ForbiddenException()
            return object : ByteArrayOutput() {
                override fun close() {
                    super.close()
                    file.body = this.toByteArray()
                }
            }.asyncOutput()
        }
    }
}

private sealed interface FileEntity {
    var name: String
    var parent: Directory?

    class File(override var name: String, override var parent: Directory?) : FileEntity {
        var body = ByteArray(0)
        var lastModified: Long = 0
    }

    class Directory(override var name: String, override var parent: Directory?) : FileEntity {
        val entities = ArrayList<FileEntity>()
    }
}
