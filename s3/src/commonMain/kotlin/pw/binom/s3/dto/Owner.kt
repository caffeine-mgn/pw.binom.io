package pw.binom.s3.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.binom.s3.AWS_NAMESPACE1
import pw.binom.s3.AWS_NAMESPACE2
import pw.binom.xml.serialization.annotations.XmlNamespace
import pw.binom.xml.serialization.annotations.XmlNode

@Serializable
data class Owner(
    @XmlNode
    @SerialName("ID")
    @XmlNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2])
    val id: String,
    @XmlNode
    @SerialName("DisplayName")
    @XmlNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2])
    val displayName: String,
)
