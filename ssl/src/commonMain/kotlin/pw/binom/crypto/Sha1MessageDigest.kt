package pw.binom.crypto

import pw.binom.io.MessageDigest
import java.security.MessageDigest as JMessageDigest

expect class Sha1MessageDigest : MessageDigest {
    constructor()
}