package com.terrier.utilities.automation.bundles.messaging.http.client;

import javax.net.ssl.X509TrustManager;
import javax.security.cert.X509Certificate;

public class NoTrustManager implements X509TrustManager {
	public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	    return null;
	}

	public void checkClientTrusted(X509Certificate[] certs, String authType) {
	}

	public void checkServerTrusted(X509Certificate[] certs, String authType) {
	}

	/* (non-Javadoc)
	 * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[], java.lang.String)
	 */
	@Override
	public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
			throws java.security.cert.CertificateException { }

	/* (non-Javadoc)
	 * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[], java.lang.String)
	 */
	@Override
	public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
			throws java.security.cert.CertificateException { }

}