@file:Suppress("DEPRECATION_ERROR")

package pw.binom.http.rest.endpoints

import org.tlsys.rest.annotations.EndpointOutput
import pw.binom.http.rest.Endpoint

interface EndpointWithResponse<OUTPUT> : EndpointOutput<OUTPUT>, Endpoint
