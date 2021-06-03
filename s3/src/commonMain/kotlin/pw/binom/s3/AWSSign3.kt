package pw.binom.s3

import pw.binom.base64.Base64
import pw.binom.crypto.HMac
import pw.binom.io.http.Headers
import pw.binom.io.http.forEachHeader

//class AWSSign3 : Signer {
//    fun sign(
//        method: String,
//        contentMd5: ByteArray,
//        contentType: String,
//        date: String,
//        header: Headers,
//        request: String,
//        keyId: String,
//        secretKey: String
//    ): String {
//        val sb = StringBuilder()
//        sb.append(method)
//            .append("\n").append(contentMd5.toHex())
//            .append("\n").append(contentType)
//            .append("\n").append(date)
//        header.forEachHeader { key, value ->
//            val k = key.toLowerCase()
//            when (k) {
//                "x-amz-acl", "x-amz-copy-source", "x-amz-copy-source-range" -> sb.append("\n")
//                    .append("${key}: $value\n")
//            }
//        }
//        sb.append("\n").append(request)
//        val hmak = HMac(algorithm = HMac.Algorithm.SHA1, key = secretKey.encodeToByteArray())
//        hmak.update(sb.toString().encodeToByteArray())
//        val result = hmak.finish()
//        return "AWS $keyId:${Base64.encode(result)}"
//    }
//
//    override fun sign() {
//        TODO("Not yet implemented")
//    }
//}
//
//fun ByteArray.toHex() =
//    joinToString("") {
//        val str = it.toUByte().toString(16)
//        if (str.length == 1) {
//            "0$str"
//        } else {
//            str
//        }
//    }