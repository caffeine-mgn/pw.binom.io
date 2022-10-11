package pw.binom.s3.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.binom.s3.AWS_NAMESPACE1
import pw.binom.s3.AWS_NAMESPACE2
import pw.binom.s3.AWS_NAMESPACE3
import pw.binom.xml.serialization.annotations.XmlName
import pw.binom.xml.serialization.annotations.XmlNamespace
import pw.binom.xml.serialization.annotations.XmlNode

@Serializable
@SerialName("Part")
@XmlNamespace([AWS_NAMESPACE3, AWS_NAMESPACE1, AWS_NAMESPACE2])
class Part(
    @XmlNamespace([AWS_NAMESPACE3, AWS_NAMESPACE1, AWS_NAMESPACE2])
    @XmlName("ChecksumCRC32")
    @XmlNode
    val checksumCRC32: String? = null,
    @XmlNamespace([AWS_NAMESPACE3, AWS_NAMESPACE1, AWS_NAMESPACE2])
    @XmlName("ChecksumCRC32C")
    @XmlNode
    val checksumCRC32C: String? = null,
    @XmlNamespace([AWS_NAMESPACE3, AWS_NAMESPACE1, AWS_NAMESPACE2])
    @XmlName("ChecksumSHA1")
    @XmlNode
    val checksumSHA1: String? = null,
    @XmlNamespace([AWS_NAMESPACE3, AWS_NAMESPACE1, AWS_NAMESPACE2])
    @XmlName("ChecksumSHA256")
    @XmlNode
    val checksumSHA256: String? = null,
    @XmlNamespace([AWS_NAMESPACE3, AWS_NAMESPACE1, AWS_NAMESPACE2])
    @XmlName("ETag")
    @XmlNode
    val eTag: String? = null,
    @XmlNamespace([AWS_NAMESPACE3, AWS_NAMESPACE1, AWS_NAMESPACE2])
    @XmlName("PartNumber")
    @XmlNode
    val partNumber: Int? = null,
)
