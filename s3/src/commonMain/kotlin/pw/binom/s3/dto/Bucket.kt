package pw.binom.s3.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import pw.binom.date.DateTime
import pw.binom.s3.AWS_NAMESPACE1
import pw.binom.s3.AWS_NAMESPACE2
import pw.binom.s3.AWS_NAMESPACE3
import pw.binom.xml.serialization.annotations.XmlSerialName
import pw.binom.xml.serialization.annotations.XmlNamespace
import pw.binom.xml.serialization.annotations.XmlNode

@XmlSerialName("Bucket")
@XmlNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2, AWS_NAMESPACE3])
@Serializable
data class Bucket(
    @XmlSerialName("Name")
    @XmlNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2, AWS_NAMESPACE3])
    @XmlNode
    val name: String,

    @XmlSerialName("CreationDate")
    @XmlNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2, AWS_NAMESPACE3])
    @Contextual
    @XmlNode
    val creationDate: DateTime,
)
