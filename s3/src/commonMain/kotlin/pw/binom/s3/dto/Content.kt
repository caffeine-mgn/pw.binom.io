package pw.binom.s3.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.binom.s3.AWS_NAMESPACE1
import pw.binom.s3.AWS_NAMESPACE2
import pw.binom.xml.serialization.annotations.XmlNamespace
import pw.binom.xml.serialization.annotations.XmlNode

@SerialName("Contents")
@XmlNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2])
@Serializable
data class Content(
    @XmlNode
    @XmlNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2])
    @SerialName("Key")
    val key: String,
    @XmlNode
    @XmlNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2])
    @SerialName("LastModified")
    val lastModified: String,
    @XmlNode
    @XmlNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2])
    @SerialName("ETag")
    val eTag: String,
    @XmlNode
    @XmlNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2])
    @SerialName("Size")
    val size: ULong,
    @XmlNode
    @XmlNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2])
    @SerialName("Owner")
    val owner: Owner? = null,
    @XmlNode
    @XmlNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2])
    @SerialName("StorageClass")
    val StorageClass: String,
)
