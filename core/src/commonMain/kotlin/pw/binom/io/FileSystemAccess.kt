package pw.binom.io

/**
 * File System Access Controller
 *
 * @param U some user type
 */
interface FileSystemAccess<U> {

    /**
     * Check access to file [path] for user [user]
     *
     * @param user User
     * @param path File Path
     */
    suspend fun getFile(user: U, path: String)

    /**
     * Check delete access to file [path] for user [user]
     *
     * @param user User
     * @param path File Path
     */
    suspend fun deleteFile(user: U, path: String)

    /**
     * Check access for show file [from] in result of [FileSystem.getDir] for user [user]
     *
     * @param user User
     * @param from source file path
     * @param to destination file path
     */
    suspend fun filterFileList(user: U, path: String): Boolean

    /**
     * Check access copy file from [from] to [to] for user [user]
     *
     * @param user User
     * @param from source file path
     * @param to destination file path
     */
    suspend fun copyFile(user: U, from: String, to: String)

    /**
     * Check access move file from [from] to [to] for user [user]
     *
     * @param user User
     * @param from source file path
     * @param to destination file path
     */
    suspend fun moveFile(user: U, from: String, to: String)

    /**
     * Check access put a new file [path] for user [user]
     *
     * @param user User
     * @param path File Path
     */
    suspend fun putFile(user: U, path: String)

    /**
     * Check access to make directory [path] for user [user]
     *
     * @param user User
     * @param path New directory path
     */
    suspend fun mkdir(user: U, path: String)

    sealed class AccessException : RuntimeException() {
        class UnauthorizedException : AccessException()
        class ForbiddenException : AccessException()
    }

    companion object
}

private object PrivateFullAccessFileSystemAccess : FileSystemAccess<Any?> {
    override suspend fun getFile(user: Any?, path: String) {
    }

    override suspend fun deleteFile(user: Any?, path: String) {
    }

    override suspend fun filterFileList(user: Any?, path: String): Boolean = true

    override suspend fun copyFile(user: Any?, from: String, to: String) {
    }

    override suspend fun moveFile(user: Any?, from: String, to: String) {
    }

    override suspend fun putFile(user: Any?, path: String) {
    }

    override suspend fun mkdir(user: Any?, path: String) {
    }
}

private object PrivateForbiddenFileSystemAccess : FileSystemAccess<Any?> {
    override suspend fun getFile(user: Any?, path: String) {
        throw FileSystemAccess.AccessException.ForbiddenException()
    }

    override suspend fun deleteFile(user: Any?, path: String) {
        throw FileSystemAccess.AccessException.ForbiddenException()
    }

    override suspend fun filterFileList(user: Any?, path: String): Boolean {
        throw FileSystemAccess.AccessException.ForbiddenException()
    }

    override suspend fun copyFile(user: Any?, from: String, to: String) {
        throw FileSystemAccess.AccessException.ForbiddenException()
    }

    override suspend fun moveFile(user: Any?, from: String, to: String) {
        throw FileSystemAccess.AccessException.ForbiddenException()
    }

    override suspend fun putFile(user: Any?, path: String) {
        throw FileSystemAccess.AccessException.ForbiddenException()
    }

    override suspend fun mkdir(user: Any?, path: String) {
        throw FileSystemAccess.AccessException.ForbiddenException()
    }
}

/**
 * Returns FileSystemAccess that allow all Access
 */
@Suppress("UNCHECKED_CAST")
fun <U> FileSystemAccess.Companion.fullAccess(): FileSystemAccess<U> = PrivateFullAccessFileSystemAccess as FileSystemAccess<U>

/**
 * Returns FileSystemAccess that disallow all Access
 */
@Suppress("UNCHECKED_CAST")
fun <U> FileSystemAccess.Companion.forbidden(): FileSystemAccess<U> = PrivateForbiddenFileSystemAccess as FileSystemAccess<U>