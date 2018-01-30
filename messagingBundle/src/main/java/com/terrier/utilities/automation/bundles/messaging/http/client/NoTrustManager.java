package com.terrier.utilities.automation.bundles.messaging.http.client;

import javax.net.ssl.X509TrustManager;
import javax.security.cert.X509Certificate;

/**
 * Manager sans vérification
 * @author PVZN02821
 *
 */
public class NoTrustManager implements X509TrustManager {
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
	public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
			throws java.security.cert.CertificateException { 
		// Trust all
	}

}