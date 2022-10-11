package pw.binom.s3.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("CompleteMultipartUpload")
class CompleteMultipartUpload(
    val parts: List<Part>
)
