package org.tlsys.rest.annotations

import kotlinx.serialization.KSerializer

interface EndpointOutput<OUTPUT> {
    val output: KSerializer<OUTPUT>
}