package com.terrier.utilities.automation.bundles.communs.http;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager sans vérification
 * @author PVZN02821
 *
 */
public class ClientHTTPTrustManager implements X509TrustManager {

	private static final String[] CERTIFICATS_CN = {
			"CN=*.mailgun.net", 
			"CN=*.free-mobile.fr", 
			"CN=slack.com",
			"CN=ovh.com"};


	private static final Logger LOGGER = LoggerFactory.getLogger( ClientHTTPTrustManager.class );

	
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
			Arrays.asList(certificates).stream().forEach(t -> LOGGER.debug(t.getSubjectX500Principal().getName()));
			throw new CertificateException("Le certificat [{}] ne correspond pas à un élément de la liste");
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
			Arrays.asList(certificates).stream().forEach(t -> LOGGER.debug(t.getSubjectX500Principal().getName()));
			throw new CertificateException("Le certificat ne correspond pas à un élément de la liste");
		}
	}
}