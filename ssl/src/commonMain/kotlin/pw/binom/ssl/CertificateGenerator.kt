package pw.binom.ssl

import pw.binom.Date

//expect object CertificateGenerator{
//    fun generate(
//            keySize:Int,
//            algorithm:CertificateAlgorithm,
//            from: Date,
//            to: Date,
//            principals: Map<String, String>,
//            serialNumber:Long,
//            subject:String?,
//            issuer:String?):Pair<PrivateCertificate,X509Certificate>
//}
//
//enum class CertificateAlgorithm{
//    RSA
//}