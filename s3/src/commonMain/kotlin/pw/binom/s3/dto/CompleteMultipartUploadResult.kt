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
@SerialName("CompleteMultipartUploadResult")
@XmlNamespace([AWS_NAMESPACE3, AWS_NAMESPACE1, AWS_NAMESPACE2])
data class CompleteMultipartUploadResult(
    @XmlSerialName("Location")
    @XmlNamespace([AWS_NAMESPACE3, AWS_NAMESPACE1, AWS_NAMESPACE2])
    @XmlNode
    val Location: String,
    @XmlSerialName("Bucket")
    @XmlNamespace([AWS_NAMESPACE3, AWS_NAMESPACE1, AWS_NAMESPACE2])
    @XmlNode
    val Bucket: String,
    @XmlSerialName("Key")
    @XmlNamespace([AWS_NAMESPACE3, AWS_NAMESPACE1, AWS_NAMESPACE2])
    @XmlNode
    val Key: String,
    @XmlSerialName("ETag")
    @XmlNamespace([AWS_NAMESPACE3, AWS_NAMESPACE1, AWS_NAMESPACE2])
    @XmlNode
    val ETag: String,
)
