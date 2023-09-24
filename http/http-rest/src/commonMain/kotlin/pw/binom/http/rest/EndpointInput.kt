package org.tlsys.rest.annotations

import kotlinx.serialization.KSerializer

interface EndpointInput<INPUT> {
    val input: KSerializer<INPUT>
}