package pw.binom.ssl

import org.bouncycastle.asn1.x509.ExtendedKeyUsage
import org.bouncycastle.asn1.x509.KeyPurposeId
import org.bouncycastle.asn1.x509.X509Extensions
import org.bouncycastle.asn1.x509.X509Name
import org.bouncycastle.jcajce.provider.asymmetric.dh.KeyPairGeneratorSpi
import org.bouncycastle.x509.X509V3CertificateGenerator
import pw.binom.date.Date
import java.math.BigInteger
import javax.security.auth.x500.X500Principal

/*
actual object CertificateGenerator {
    actual fun generate(
            keySize: Int,
            algorithm: CertificateAlgorithm,
            from: Date,
            to: Date,
            principals: Map<String, String>,
            serialNumber: Long,
            subject: String?,
            issuer: String?): Pair<PrivateCertificate, X509Certificate> {

        val keyPairGenerator = KeyPairGeneratorSpi.getInstance("RSA")
        keyPairGenerator.initialize(keySize)
        val keyPair = keyPairGenerator.generateKeyPair()
        val certGen = X509V3CertificateGenerator()

        certGen.setSerialNumber(BigInteger.valueOf(serialNumber))


        if (subject != null)
            certGen.setSubjectDN(X509Name(subject))

        if (issuer != null)
            certGen.setIssuerDN(X500Principal(issuer))
        certGen.setNotBefore(java.util.Date(from.time))
        certGen.setNotAfter(java.util.Date(to.time))


        certGen.setPublicKey(keyPair.public)
        certGen.setSignatureAlgorithm("SHA512withRSA")
        certGen.addExtension(X509Extensions.ExtendedKeyUsage, true,
                ExtendedKeyUsage(KeyPurposeId.id_kp_timeStamping))
//
        // finally, sign the certificate with the private key of the same KeyPair
        val cert = certGen.generate(keyPair.private)
        return PrivateCertificate(keyPair.private) to X509Certificate(cert)

//
//
//        val keyGen = KeyPairGenerator.getInstance(algorithm.toJava())
//        keyGen.initialize(keySize)
//        val pair = keyGen.genKeyPair()
//
//        val info = X509CertInfo()
//
//        info.set(X509CertInfo.VALIDITY, CertificateValidity(java.util.Date(from.time), java.util.Date(to.time)));
//        info.set(X509CertInfo.SERIAL_NUMBER, CertificateSerialNumber(BigInteger.valueOf(serialNumber)))
//        info.set(X509CertInfo.SUBJECT, "SUBJECT")
//        info.set(X509CertInfo.ISSUER, "ISSUER")
//        info.set(X509CertInfo.KEY, CertificateX509Key(pair.getPublic()))
//        info.set(X509CertInfo.VERSION, CertificateVersion(CertificateVersion.V3))

//        return PrivateCertificate(pair.private) to X509Certificate(pair.public)
    }
}
*/
