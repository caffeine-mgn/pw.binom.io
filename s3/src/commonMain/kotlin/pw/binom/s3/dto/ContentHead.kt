package pw.binom.s3.dto

import pw.binom.date.DateTime

data class ContentHead(
    val region: String?,
    val length: Long?,
    val contentType: String?,
    val eTag: String?,
    val lastModify: DateTime?
)
