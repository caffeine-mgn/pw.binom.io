@file:Suppress("DEPRECATION_ERROR")

package pw.binom.http.rest.endpoints

import org.tlsys.rest.annotations.EndpointInput
import org.tlsys.rest.annotations.EndpointOutput
import pw.binom.http.rest.Endpoint

interface EndpointWithRequestAndResponse<INPUT, OUTPUT> : EndpointInput<INPUT>, EndpointOutput<OUTPUT>, Endpoint
