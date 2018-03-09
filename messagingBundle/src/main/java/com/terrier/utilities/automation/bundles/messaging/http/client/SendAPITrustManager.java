package com.terrier.utilities.automation.bundles.messaging.http.client;

import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

/**
 * Manager sans vérification
 * @author PVZN02821
 *
 */
public class SendAPITrustManager implements X509TrustManager {

	private static final String[] CERTIFICATS_CN = {"CN=*.mailgun.net", "CN=*.free-mobile.fr" };

	public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[0];
	}



	/* (non-Javadoc)
	 * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[], java.lang.String)
	 */
	@Override
	public void checkClientTrusted(X509Certificate[] certificates, String arg1) throws CertificateException {		
		// Trust all in CERTIFICATS_CN
		if(Arrays.asList(certificates)
				.stream()
				.noneMatch(cert -> Arrays.asList(CERTIFICATS_CN)
									.stream()
									.anyMatch(trusted -> cert.getSubjectX500Principal().getName().contains(trusted))
		)){		
			throw new CertificateException("Le certificat ne correspond pas à un élément de la liste");
		}
	}

	/* (non-Javadoc)
	 * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[], java.lang.String)
	 */
	@Override
	public void checkServerTrusted(X509Certificate[] certificates, String arg1)
			throws CertificateException { 
		// Trust all in CERTIFICATS_CN
		if(Arrays.asList(certificates)
				.stream()
				.noneMatch(cert -> Arrays.asList(CERTIFICATS_CN)
									.stream()
									.anyMatch(trusted -> cert.getSubjectX500Principal().getName().contains(trusted))
		)){		
			throw new CertificateException("Le certificat ne correspond pas à un élément de la liste");
		}
	}
}