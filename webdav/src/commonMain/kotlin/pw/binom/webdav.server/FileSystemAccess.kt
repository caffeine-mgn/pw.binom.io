package pw.binom.webdav.server

interface FileSystemAccess<U> {
    suspend fun getFile(user: U, path: String)
    suspend fun deleteFile(user: U, path: String)
    suspend fun filterFileList(user: U, path:String):Boolean
    suspend fun copyFile(user: U, from: String, to: String)
    suspend fun moveFile(user: U, from: String, to: String)
    suspend fun putFile(user: U, path: String)
    suspend fun mkdir(user: U, path: String)

    sealed class AccessException : RuntimeException() {
        class UnauthorizedException : AccessException()
        class ForbiddenException : AccessException()
    }
}