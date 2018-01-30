/**
 * 
 */
package com.terrier.utilities.automation.bundles.messaging.http.client;



import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import com.sun.jersey.core.util.MultivaluedMapImpl;
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

	private HostnameVerifier allHostsValid = (String hostname, SSLSession session) -> { return true; };


	/**
	 * Créé un client HTTP 
	 * (dans une méthode séparée pour pouvoir être mocké facilement)
	 * @return client HTTP
	 * @throws NoSuchAlgorithmException 
	 */
	public Client getClient() {

		ClientConfig config = new DefaultClientConfig();
		try {
			TrustManager[] trustAllCerts = new TrustManager[] { new NoTrustManager() };
			// Install the all-trusting trust manager
			SSLContext sslcontext = SSLContext.getInstance("TLS");
			sslcontext.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

			javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
			config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(allHostsValid, sslcontext));

		} catch (NoSuchAlgorithmException | KeyManagementException e1) {
			LOGGER.error("Erreur lors de la configuration SSL du client HTTP");
		}
		try{
			return Client.create(config);
		}
		catch(Exception e){
			this.service.sendNotificationEmail("Erreur envoi ", "Erreur lors de la création du Client HTTP " + e.getMessage());
			this.service.sendNotificationSMS( "Erreur lors de la création du Client HTTP " + e.getMessage());
			return null;
		}
	}

	/**
	 * @param filter
	 * @return client avec filtre
	 */
	protected Client getClient(ClientFilter filter){
		Client client = getClient();
		if(client != null){
			client.addFilter(filter);
		}
		return client;
	}


	/**
	 * Appel POST 
	 * @param clientHTTP client utilisé
	 * @param url url appelée
	 * @param formData data envoyées
	 * @return
	 */
	public boolean callHTTPPost(Client clientHTTP, String url, MultivaluedMapImpl formData){
		boolean resultat;
		LOGGER.debug("[HTTP POST] Appel de l'URI [{}]", url);
		try{
			WebResource.Builder webResource =
					clientHTTP.resource(url).type(MediaType.APPLICATION_FORM_URLENCODED);


			ClientResponse response = webResource.post(ClientResponse.class, formData);
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
	public boolean callHTTPGet(Client clientHTTP, String url, String... urlParams){
		LOGGER.debug("[HTTP GET] Appel de l'URI [{}]", url);
		boolean resultat;
		try{
			StringBuilder urlComplete = new StringBuilder(url);
			for (String param : urlParams) {
				urlComplete.append(param);
			}

			WebResource.Builder webResource = clientHTTP.resource(urlComplete.toString()).type(MediaType.APPLICATION_FORM_URLENCODED);
			ClientResponse response = webResource.get(ClientResponse.class);
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
