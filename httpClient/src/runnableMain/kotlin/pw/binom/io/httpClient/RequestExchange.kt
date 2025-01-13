package pw.binom.io.httpClient

import pw.binom.io.AsyncCloseable
import pw.binom.io.http.HttpInput
import pw.binom.io.http.HttpOutput

interface RequestExchange : HttpInput, HttpOutput, AsyncCloseable {

}
