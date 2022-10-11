package pw.binom.s3.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.binom.s3.AWS_NAMESPACE1
import pw.binom.s3.AWS_NAMESPACE2
import pw.binom.s3.AWS_NAMESPACE3
import pw.binom.xml.serialization.annotations.XmlName
import pw.binom.xml.serialization.annotations.XmlNamespace
import pw.binom.xml.serialization.annotations.XmlNode

@SerialName("CreateBucketConfiguration")
@XmlNamespace([AWS_NAMESPACE3, AWS_NAMESPACE1, AWS_NAMESPACE2])
@Serializable
class CreateBucketConfiguration(
    @XmlNamespace([AWS_NAMESPACE3, AWS_NAMESPACE1, AWS_NAMESPACE2])
    @XmlName("LocationConstraint")
    @XmlNode
    val locationConstraint: String? = null
)
