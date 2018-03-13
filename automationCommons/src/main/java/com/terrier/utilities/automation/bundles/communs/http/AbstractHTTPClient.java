/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.http;



import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.communs.enums.statut.StatutPropertyBundleEnum;

/**
 * Classe d'un client HTTP
 * @author vzwingma
 *
 */
public abstract class AbstractHTTPClient {


	private static final Logger LOGGER = LoggerFactory.getLogger( AbstractHTTPClient.class );

	// Résultat du dernier appel HTTP
	private int lastResponseCode;




	public Client getClient(){
		return getClient(null);
	}

	/**
	 * Créé un client HTTP 
	 * (dans une méthode séparée pour pouvoir être mocké facilement)
	 * @return client HTTP
	 * @throws NoSuchAlgorithmException 
	 */
	public Client getClient(HttpAuthenticationFeature feature) {

		ClientConfig clientConfig = new ClientConfig();
		if(feature != null){
			clientConfig.register(feature);
		}
		
		try {
			// Install the all-trusting trust manager
			SSLContext sslcontext = SSLContext.getInstance("TLS");

			sslcontext.init(null,  new TrustManager[] { new ClientHTTPTrustManager() }, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
			return ClientBuilder.newBuilder()
					.sslContext(sslcontext)
					.withConfig(clientConfig)
					.build();
		}
		catch(Exception e){
			LOGGER.error("Erreur envoi : Erreur lors de la création du Client HTTP {}", e.getMessage());
			return ClientBuilder.newClient(clientConfig);
		}
	}

	
	/**
	 * @param clientHTTP
	 * @param url
	 * @param path
	 * @param type
	 * @return
	 */
	public Invocation.Builder getInvocation(Client clientHTTP, String url, String path, MediaType type){
		if(clientHTTP != null){
			LOGGER.debug("[HTTP] Appel de l'URI [{}{}]", url, path != null ? path : "");
			WebTarget wt = clientHTTP.target(url);
			if(path != null){
				wt = wt.path(path);
			}
			return wt.request(type);
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
	public boolean callHTTPPost(Invocation.Builder invocation, Entity<?> entityData){
		boolean resultat;
		LOGGER.debug("[HTTP POST] Appel du service");
		try{
			invocation.header("Content-type", "application/json");
			Response response = invocation.post(entityData);
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
		LOGGER.debug("[HTTP GET] Appel du service");
		boolean resultat;
		try{

			Response response = invocation.get();
			LOGGER.debug("[HTTP GET] Resultat : {}", response.getStatus());
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
	
	
	/**
	 * Appel HTTP GET
	 * @param clientHTTP client HTTP
	 * @param url racine de l'URL
	 * @param urlParams paramètres de l'URL (à part pour ne pas les tracer)
	 * @return résultat de l'appel
	 */
	public Response callHTTPGetData(Invocation.Builder invocation){
		LOGGER.debug("[HTTP GET] Appel du service");
		try{

			Response response = invocation.get();
			LOGGER.debug("[HTTP GET] Resultat : {} / MediaType {}", response, response.getMediaType());
			this.lastResponseCode = response != null ? response.getStatus() : 0;
			return response;
		}
		catch(Exception e){
			LOGGER.error("> Resultat : Erreur lors de l'appel HTTP GET", e);
			this.lastResponseCode = 500;
			return null;
		}
	}


	/**
	 * @return the lastResponseCode
	 */
	public int getLastResponseCode() {
		return lastResponseCode;
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
