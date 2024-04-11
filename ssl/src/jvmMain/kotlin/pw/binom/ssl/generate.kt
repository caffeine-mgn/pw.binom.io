package pw.binom.ssl

import org.bouncycastle.asn1.x509.X509Name
import org.bouncycastle.x509.X509V1CertificateGenerator
import org.bouncycastle.x509.X509V3CertificateGenerator
import java.math.BigInteger
import javax.security.auth.x500.X500Principal

actual fun X509Builder.generate(): X509Certificate {
  when (version) {
    X509Builder.Version.V1 -> {
//          X509v1CertificateBuilder(
//            X500Name(issuer),
//            BigInteger.valueOf(serialNumber),
//            java.util.Date(notBefore.time),
//            java.util.Date(notAfter.time),
//            X500Name(subject),
//            TODO(),
//          )
      val certGen = X509V1CertificateGenerator()
      certGen.setSerialNumber(BigInteger.valueOf(serialNumber))

      certGen.setSubjectDN(X509Name(subject))
      certGen.setIssuerDN(X500Principal(issuer))
      certGen.setNotBefore(java.util.Date(notBefore.milliseconds))
      certGen.setNotAfter(java.util.Date(notAfter.milliseconds))

      certGen.setPublicKey(pair.native.public)
      return X509Certificate(certGen.generate(sign.native))
    }
    X509Builder.Version.V3 -> {
      val certGen = X509V3CertificateGenerator()
      certGen.setSerialNumber(BigInteger.valueOf(serialNumber))

      certGen.setSubjectDN(X509Name(subject))

      certGen.setIssuerDN(X500Principal(issuer))

      certGen.setNotBefore(java.util.Date(notBefore.milliseconds))
      certGen.setNotAfter(java.util.Date(notAfter.milliseconds))

      certGen.setPublicKey(pair.native.public)
      certGen.setSignatureAlgorithm("SHA512withRSA")
      return X509Certificate(certGen.generate(sign.native))
    }
  }
}
