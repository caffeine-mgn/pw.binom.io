package pw.binom.webdav.server

import pw.binom.io.*
import pw.binom.io.file.*

class LocalFileSystem<U>(val root: File, val access: FileSystemAccess<U>) : FileSystem<U> {
    override suspend fun copy(user: U, from: String, to: String) {
        access.copyFile(user, from = from, to = to)
        val fromFile = File(root, from.removePrefix("/"))
        val toFile = File(root, to.removePrefix("/"))

        if (!fromFile.isExist)
            throw FileSystem.FileNotFoundException(from)

        FileInputStream(fromFile).use { s ->
            FileOutputStream(toFile).use { d ->
                s.copyTo(d)
            }
        }
    }

    override suspend fun move(user: U, from: String, to: String) {
        access.moveFile(user, from, to)
        val fromFile = File(root, from.removePrefix("/"))
        val toFile = File(root, to.removePrefix("/"))

        if (!fromFile.isExist)
            throw FileSystem.FileNotFoundException(from)

        fromFile.renameTo(toFile)
    }

    override suspend fun rewriteFile(user: U, path: String): AsyncOutputStream {
        access.putFile(user, path)
        val file = File(root, path)
        file.parent?.mkdirs()
        return FileOutputStream(file, false).asAsync()
    }

    override suspend fun getEntry(user: U, path: String): FileSystem.Entity? {
        access.getFile(user, path)
        val f = File(root, path.removePrefix("/"))
        if (!f.isExist)
            return null
        return EntityImpl(f)
    }

    override suspend fun read(user: U, path: String): AsyncInputStream? {
        access.getFile(user, path)
        val file = File(root, path.removePrefix("/"))
        if (!file.isFile)
            return null

        return FileInputStream(file).asAsync()
    }

    override suspend fun delete(user: U, path: String) {
        access.deleteFile(user, path)
        File(root, path.removePrefix("/")).deleteRecursive()
    }

    override suspend fun mkdir(user: U, path: String) {
        access.mkdir(user, path)
        File(root, path.removePrefix("/")).mkdirs()
    }

    override suspend fun getEntities(user: U, path: String): List<FileSystem.Entity>? {
        if (path == "/")
            return root.listEntities(user)

        val f = File(root, path.removePrefix("/"))
        if (f.isDirectory)
            return f.listEntities(user)
        return null
    }

    private suspend fun File.listEntities(user: U): List<EntityImpl> {
        val out = ArrayList<EntityImpl>()
        this.iterator().use {
            it.forEach {
                if (access.filterFileList(user, it.path.removePrefix(root.path)))
                    out += EntityImpl(it)
            }
        }

        return out
    }

    private inner class EntityImpl(val file: File) : FileSystem.Entity {
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