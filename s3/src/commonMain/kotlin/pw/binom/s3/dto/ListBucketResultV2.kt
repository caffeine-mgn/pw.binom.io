package pw.binom.s3.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.binom.s3.AWS_NAMESPACE1
import pw.binom.s3.AWS_NAMESPACE2
import pw.binom.s3.AWS_NAMESPACE3
import pw.binom.xml.serialization.annotations.XmlEmptyValue
import pw.binom.xml.serialization.annotations.XmlNamespace
import pw.binom.xml.serialization.annotations.XmlNode

@XmlNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2, AWS_NAMESPACE3])
@Serializable
@SerialName("ListBucketResult")
data class ListBucketResultV2(
    @XmlNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2, AWS_NAMESPACE3])
    @SerialName("Name")
    @XmlNode
    val name: String,
    @XmlEmptyValue("")
    @XmlNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2, AWS_NAMESPACE3])
    @SerialName("Prefix")
    @XmlNode
    val prefix: String = "",
    @XmlNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2, AWS_NAMESPACE3])
    @SerialName("NextContinuationToken")
    @XmlNode
    val nextContinuationToken: String? = null,
    @XmlNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2, AWS_NAMESPACE3])
    @SerialName("KeyCount")
    @XmlNode
    val keyCount: Int,
    @XmlNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2, AWS_NAMESPACE3])
    @SerialName("MaxKeys")
    @XmlNode
    val maxKeys: Int,
    @XmlNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2, AWS_NAMESPACE3])
    @SerialName("Delimiter")
    @XmlNode
    val delimiter: String = "",
    @XmlNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2, AWS_NAMESPACE3])
    @SerialName("IsTruncated")
    @XmlNode
    val isTruncated: Boolean,
    @XmlNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2, AWS_NAMESPACE3])
    @SerialName("Contents")
    val contents: List<Content>
)
