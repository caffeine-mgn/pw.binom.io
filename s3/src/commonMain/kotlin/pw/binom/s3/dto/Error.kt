package pw.binom.s3.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.binom.s3.AWS_NAMESPACE1
import pw.binom.s3.AWS_NAMESPACE2
import pw.binom.s3.AWS_NAMESPACE3
import pw.binom.xml.serialization.annotations.XmlName
import pw.binom.xml.serialization.annotations.XmlNamespace

@SerialName("Error")
@XmlNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2, AWS_NAMESPACE3])
@Serializable
data class Error(
    @XmlNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2, AWS_NAMESPACE3])
    @XmlName("Code")
    val key: String? = null,

    @XmlNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2, AWS_NAMESPACE3])
    @XmlName("Message")
    val message: String? = null,

    @XmlNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2, AWS_NAMESPACE3])
    @XmlName("VersionId")
    val versionId: String? = null,
)
