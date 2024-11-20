package `in`.testpress.util

import android.content.Context
import `in`.testpress.R
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * TrustFactory - Manages SSL/TLS connections by providing a custom SSLSocketFactory
 * and X509TrustManager. This class includes root certificates for the ISRG Root X1 and
 * ISRG Root X2, used to ensure compatibility with older Android devices that do not
 * natively trust the ISRG Root X1. This setup is due to the expiration of cross-signatures
 * by IdenTrust's DST Root CA X3, which affects older Android versions.
 *
 * For more information on the expiration of the cross-signature, refer to:
 * [Let’s Encrypt Cross-Sign Expiration](https://letsencrypt.org/2023/07/10/cross-sign-expiration),
 * [Stackoverflow](https://stackoverflow.com/questions/64844311/certpathvalidatorexception-connecting-to-a-lets-encrypt-host-on-android-m-or-ea/78309587#78309587)
 */

class TrustFactory {
    companion object {
        fun getTrustFactoryManager(context: Context): Pair<SSLSocketFactory, X509TrustManager> {
            val cf = CertificateFactory.getInstance("X.509")

            val isrgRoot1Input = context.resources.openRawResource(R.raw.isrg_root_x1)
            val isrgRoot1Certificate: Certificate = isrgRoot1Input.use {
                cf.generateCertificate(it)
            }

            val isrgRoot2Input = context.resources.openRawResource(R.raw.isrg_root_x2)
            val isrgRoot2Certificate: Certificate = isrgRoot2Input.use {
                cf.generateCertificate(it)
            }

            val keyStoreType = KeyStore.getDefaultType()
            val keyStore = KeyStore.getInstance(keyStoreType).apply {
                load(null, null)
                setCertificateEntry("isrgrootx1", isrgRoot1Certificate)
                setCertificateEntry("isrgrootx2", isrgRoot2Certificate)
            }

            val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
            val tmf = TrustManagerFactory.getInstance(tmfAlgorithm).apply {
                init(keyStore)
            }

            val sslContext = SSLContext.getInstance("TLS").apply {
                init(null, tmf.trustManagers, null)
            }

            return Pair(sslContext.socketFactory, tmf.trustManagers[0] as X509TrustManager)
        }
    }
}