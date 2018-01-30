/**
 * 
 */
package com.terrier.utilities.automation.bundles.messaging.http.client;



import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.communs.enums.statut.StatutPropertyBundleEnum;
import com.terrier.utilities.automation.bundles.communs.model.StatutPropertyBundleObject;
import com.terrier.utilities.automation.bundles.messaging.MessagingBusinessService;

/**
 * Classe d'un client HTTP
 * @author vzwingma
 *
 */
public abstract class AbstractHTTPClientRunnable implements Runnable {


	private static final Logger LOGGER = LoggerFactory.getLogger( AbstractHTTPClientRunnable.class );

	// Résultat du dernier appel HTTP
	private int lastResponseCode;

	// Service Métier
	private MessagingBusinessService service;

	private HostnameVerifier allHostsValid = (String hostname, SSLSession session) -> true ;


	/**
	 * Créé un client HTTP 
	 * (dans une méthode séparée pour pouvoir être mocké facilement)
	 * @return client HTTP
	 * @throws NoSuchAlgorithmException 
	 */
	public Client getClient() {


		try {
			// Install the all-trusting trust manager
			SSLContext sslcontext = SSLContext.getInstance("TLS");

			sslcontext.init(null,  new TrustManager[] { new NoTrustManager() }, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
			return ClientBuilder.newBuilder().sslContext(sslcontext)
					.hostnameVerifier(allHostsValid)
					.build();
		}
		catch(Exception e){
			this.service.sendNotificationEmail("Erreur envoi ", "Erreur lors de la création du Client HTTP " + e.getMessage());
			this.service.sendNotificationSMS( "Erreur lors de la création du Client HTTP " + e.getMessage());
			return null;
		}
	}

	
	public Invocation.Builder getInvocation(Client clientHTTP, String url, String path, MediaType type){
		if(clientHTTP != null){
			System.err.println(clientHTTP);
			System.err.println(clientHTTP.target(url));
			System.err.println(clientHTTP.target(url).path(path));
			return clientHTTP.target(url).path(path).request(type);
		}
		return null;
	}

	/**
	 * Appel POST 
	 * @param clientHTTP client utilisé
	 * @param url url appelée
	 * @param formData data envoyées
	 * @return
	 */
	public boolean callHTTPPost(Invocation.Builder invocation, MultivaluedMap<String, String> formData){
		boolean resultat;
		LOGGER.debug("[HTTP POST] Appel de l'URI [{}]", invocation);
		try{
			Response response = invocation.post(Entity.form(formData));
			LOGGER.debug("[HTTP POST] Resultat : {}", response);
			if(response != null){
				this.lastResponseCode = response.getStatus();
			}
			else{
				this.lastResponseCode = 500;
			}
		}
		catch(Exception e){
			LOGGER.error("> Resultat : Erreur lors de l'appel HTTP POST", e);
			this.lastResponseCode = 500;
		}
		resultat = this.lastResponseCode == 200;
		return resultat;
	}


	/**
	 * Appel HTTP GET
	 * @param clientHTTP client HTTP
	 * @param url racine de l'URL
	 * @param urlParams paramètres de l'URL (à part pour ne pas les tracer)
	 * @return résultat de l'appel
	 */
	public boolean callHTTPGet(Invocation.Builder invocation){
		LOGGER.debug("[HTTP GET] Appel de l'URI [{}]", invocation);
		boolean resultat;
		try{

			Response response = invocation.get();
			LOGGER.debug("[HTTP GET] Resultat : {}", response);
			this.lastResponseCode = response != null ? response.getStatus() : 0;
			resultat = response != null && response.getStatus() == 200;
		}
		catch(Exception e){
			LOGGER.error("> Resultat : Erreur lors de l'appel HTTP GET", e);
			resultat = false;
			this.lastResponseCode = 500;
		}
		return resultat;
	}


	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		HTTPClientRun();
	}

	/**
	 * Méthode de traitement runnable
	 */
	public abstract void HTTPClientRun();




	/**
	 * @return the lastResponseCode
	 */
	public int getLastResponseCode() {
		return lastResponseCode;
	}

	/**
	 * Ajout des informations du bundle à superviser
	 * @param supervisionEvents événements de supervision, sous la forme titre->Données
	 */
	public abstract void updateSupervisionEvents(List<StatutPropertyBundleObject> supervisionEvents);


	/**
	 * @param service the service to set
	 */
	public void setService(MessagingBusinessService service) {
		this.service = service;
	}

	/**
	 * @return the service
	 */
	public MessagingBusinessService getService() {
		return service;
	}

	/**
	 * @param code
	 * @return code Statut correspondant
	 */
	public static StatutPropertyBundleEnum getCode(int code){
		switch (code) {
		case 200:
			return StatutPropertyBundleEnum.OK;
		case 0:
			return StatutPropertyBundleEnum.WARNING;
		default:
			return StatutPropertyBundleEnum.ERROR;
		}
	}
}
