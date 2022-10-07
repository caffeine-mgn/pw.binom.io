package pw.binom.s3.dto

import kotlinx.serialization.Serializable
import pw.binom.s3.AWS_NAMESPACE1
import pw.binom.s3.AWS_NAMESPACE2
import pw.binom.s3.AWS_NAMESPACE3
import pw.binom.xml.serialization.annotations.*

@XmlNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2, AWS_NAMESPACE3])
@XmlName("ListAllMyBucketsResult")
@Serializable
data class ListAllMyBucketsResult(
    @XmlNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2, AWS_NAMESPACE3])
    @XmlName("Owner")
    val owner: Owner,

    @XmlWrapperNamespace([AWS_NAMESPACE1, AWS_NAMESPACE2, AWS_NAMESPACE3])
    @XmlWrapper("Buckets")
    val buckets: List<Bucket>
)
