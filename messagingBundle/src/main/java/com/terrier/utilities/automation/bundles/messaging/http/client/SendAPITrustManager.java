package com.terrier.utilities.automation.bundles.messaging.http.client;

import java.util.Arrays;

import javax.net.ssl.X509TrustManager;
import javax.security.cert.X509Certificate;

/**
 * Manager sans vérification
 * @author PVZN02821
 *
 */
public class SendAPITrustManager implements X509TrustManager {

	private static final String[] CERTIFICATS_CN = {"CN=*.mailgun.net", "CN=*.free-mobile.fr" };

	public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		return new java.security.cert.X509Certificate[0];
	}

	public void checkClientTrusted(X509Certificate[] certs, String authType) {
		// Trust all
	}

	public void checkServerTrusted(X509Certificate[] certs, String authType) {
		// Trust all
	}

	/* (non-Javadoc)
	 * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[], java.lang.String)
	 */
	@Override
	public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1) throws java.security.cert.CertificateException {		
		// Trust all
	}

	/* (non-Javadoc)
	 * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[], java.lang.String)
	 */
	@Override
	public void checkServerTrusted(java.security.cert.X509Certificate[] certificates, String arg1)
			throws java.security.cert.CertificateException { 
		// Trust all in CERTIFICATS_CN
		if(!Arrays.asList(certificates)
				.stream()
				.anyMatch(cert -> {
					return Arrays.asList(CERTIFICATS_CN)
							.stream()
							.anyMatch(trusted -> cert.getSubjectX500Principal().getName().contains(trusted));
		})){		
			throw new java.security.cert.CertificateException("Le certificat ne correspond pas à un élément de la liste");
		}
	}

}