package pw.binom.io.file

import pw.binom.io.*

class LocalFileSystem<U>(val root: File, val access: FileSystemAccess<U>) : FileSystem<U> {
    override suspend fun new(user: U, path: String): AsyncOutputStream {
        access.putFile(user, path)
        val file = File(root, path.removePrefix("/"))
        file.parent?.mkdirs()
        return FileOutputStream(file).asAsync()
    }

    override suspend fun get(user: U, path: String): FileSystem.Entity<U>? {
        access.getFile(user, path)
        val f = File(root, path.removePrefix("/"))
        if (!f.isExist)
            return null
        return EntityImpl(f, user)
    }

    override suspend fun mkdir(user: U, path: String): FileSystem.Entity<U> {
        access.mkdir(user, path)
        val file = File(root, path.removePrefix("/"))
        file.mkdirs()
        return EntityImpl(file, user)
    }


    override suspend fun getDir(user: U, path: String): Sequence<FileSystem.Entity<U>>? {
        if (path == "/")
            return root.listEntities(user)

        val f = File(root, path.removePrefix("/"))
        if (f.isDirectory)
            return f.listEntities(user)
        return null
    }

    private suspend fun File.listEntities(user: U): Sequence<EntityImpl> {
        val out = ArrayList<EntityImpl>()
        this.iterator().use {
            it.forEach {
                if (access.filterFileList(user, it.path.removePrefix(root.path)))
                    out += EntityImpl(it, user)
            }
        }

        return out.asSequence()
    }

    private inner class EntityImpl(val file: File, override val user: U) : FileSystem.Entity<U> {
        override val fileSystem: FileSystem<U>
            get() = this@LocalFileSystem

        override suspend fun read(): AsyncInputStream? {
            access.getFile(user, path)
            val file = File(root, path.removePrefix("/"))
            if (!file.isFile)
                return null

            return FileInputStream(file).asAsync()
        }

        override suspend fun copy(path: String, overwrite: Boolean): FileSystem.Entity<U> {
            access.copyFile(user, from = this.path, to = path)
//            val fromFile = File(root, from.removePrefix("/"))
            val toFile = File(root, path.removePrefix("/"))

            if (toFile.isExist && !overwrite)
                throw FileSystem.EntityExistException(path)

            if (!file.isExist)
                throw FileSystem.FileNotFoundException(this.path)

            FileInputStream(this.file).use { s ->
                FileOutputStream(toFile).use { d ->
                    s.copyTo(d)
                }
            }
            return EntityImpl(toFile, user)
        }

        override suspend fun move(path: String, overwrite: Boolean): FileSystem.Entity<U> {
            access.moveFile(user, this.path, path)
            val toFile = File(root, path.removePrefix("/"))

            if (toFile.isExist && !overwrite)
                throw FileSystem.EntityExistException(path)

            if (!file.isExist)
                throw FileSystem.FileNotFoundException(this.path)

            file.renameTo(toFile)
            return EntityImpl(toFile, user)
        }

        override suspend fun delete() {
            access.deleteFile(user, path)
            File(root, path.removePrefix("/")).deleteRecursive()
        }

        override suspend fun rewrite(): AsyncOutputStream {
            access.putFile(user, path)
            val file = File(root, path)
            file.parent?.mkdirs()
            return FileOutputStream(file, false).asAsync()
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