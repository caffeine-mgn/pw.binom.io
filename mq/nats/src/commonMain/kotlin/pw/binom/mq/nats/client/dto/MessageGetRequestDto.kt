package pw.binom.mq.nats.client.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageGetRequestDto(
  @SerialName("seq")
  val sequence: Long,
  @SerialName("last_by_subj")
  val lastBySubject: String?,
  @SerialName("next_by_subj")
  val nextBySubject: String?,
) {
  val isLastBySubject
    get() = lastBySubject != null

  companion object {
    fun forSequence(sequence: Long) = MessageGetRequestDto(sequence = sequence, lastBySubject = null, nextBySubject = null)

    fun lastForSubject(subject: String) = MessageGetRequestDto(sequence = -1, lastBySubject = subject, nextBySubject = null)

    fun firstForSubject(subject: String) = MessageGetRequestDto(sequence = -1, lastBySubject = null, nextBySubject = subject)

    fun nextForSubject(
      sequence: Long,
      subject: String,
    ) = MessageGetRequestDto(sequence = -1, lastBySubject = null, nextBySubject = subject)
  }
}
