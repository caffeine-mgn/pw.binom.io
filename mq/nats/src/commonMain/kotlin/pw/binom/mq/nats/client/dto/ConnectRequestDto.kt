package pw.binom.mq.nats.client.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConnectRequestDto(
  /**
   * Turns on `+OK` protocol acknowledgements.
   */
  @SerialName("verbose")
  val verbose: Boolean,
  /**
   * Turns on additional strict format checking, e.g. for properly formed subjects.
   */
  @SerialName("pedantic")
  val pedantic: Boolean,
  /**
   * Indicates whether the client requires an SSL connection.
   */
  @SerialName("tls_required")
  val tlsRequired: Boolean,
  /**
   * Client authorization token.
   */
  @SerialName("auth_token")
  val authToken: String? = null,
  /**
   * Connection username.
   */
  @SerialName("user")
  val user: String? = null,
  /**
   * Connection password.
   */
  @SerialName("pass")
  val pass: String? = null,
  /**
   * Client name.
   */
  @SerialName("name")
  val name: String? = null,
  /**
   * The implementation language of the client.
   */
  @SerialName("lang")
  val lang: String,
  /**
   * The version of the client.
   */
  @SerialName("version")
  val version: String,
  /**
   *Sending 0 (or absent) indicates client supports original protocol. Sending 1 indicates that the client supports
   * dynamic reconfiguration of cluster topology changes by asynchronously receiving `INFO` messages with known
   * servers it can reconnect to.
   */
  @SerialName("protocol")
  val protocol: Int? = null,
  /**
   * If set to false, the server (version 1.2.0+) will not send originating messages from this connection to its own
   * subscriptions. Clients should set this to false only for server supporting this feature, which is when proto in
   * the INFO protocol is set to at least 1.
   */
  @SerialName("echo")
  val echo: Boolean? = null,
  /**
   * In case the server has responded with a nonce on INFO, then a NATS client must use this field to reply with the
   * signed nonce.
   */
  @SerialName("sig")
  val sig: String? = null,
  /**
   * The JWT that identifies a user permissions and account.
   */
  @SerialName("jwt")
  val jwt: String? = null,
  /**
   * Enable [quick replies for cases where a request is sent to a topic with no responders](https://docs.nats.io/nats-concepts/core-nats/reqreply#no-responders).
   */
  @SerialName("no_responders")
  val noResponders: Boolean? = null,
  /**
   * Whether the client supports headers.
   */
  @SerialName("headers")
  val headers: Boolean? = null,
  /**
   * The public NKey to authenticate the client. This will be used to verify the signature (sig) against the nonce
   * provided in the INFO message.
   */
  @SerialName("nkey")
  val nkey: String? = null,
)
