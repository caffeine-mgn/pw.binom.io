package pw.binom.s3.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.binom.s3.AWS_NAMESPACE1
import pw.binom.s3.AWS_NAMESPACE2
import pw.binom.s3.AWS_NAMESPACE3
import pw.binom.xml.serialization.annotations.XmlSerialName
import pw.binom.xml.serialization.annotations.XmlNamespace
import pw.binom.xml.serialization.annotations.XmlNode

@Serializable
@SerialName("InitiateMultipartUploadResult")
@XmlNamespace([AWS_NAMESPACE3, AWS_NAMESPACE1, AWS_NAMESPACE2])
class InitiateMultipartUploadResult(
    @XmlNode @XmlSerialName("Bucket") @XmlNamespace([AWS_NAMESPACE3, AWS_NAMESPACE1, AWS_NAMESPACE2])
    val bucket: String,

    @XmlNode @XmlSerialName("Key") @XmlNamespace([AWS_NAMESPACE3, AWS_NAMESPACE1, AWS_NAMESPACE2])
    val key: String,

    @XmlNode @XmlSerialName("UploadId") @XmlNamespace([AWS_NAMESPACE3, AWS_NAMESPACE1, AWS_NAMESPACE2])
    val uploadId: String,
)
