@file:Suppress("DEPRECATION_ERROR")

package pw.binom.http.rest.endpoints

import org.tlsys.rest.annotations.EndpointInput
import pw.binom.http.rest.Endpoint

interface EndpointWithRequest<INTPUT> : EndpointInput<INTPUT>, Endpoint
