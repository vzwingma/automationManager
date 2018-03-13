/**
 * 
 */
package com.terrier.utilities.automation.bundles.emails.worker.business;

import java.io.InputStream;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.communs.http.AbstractHTTPClient;

/**
 * @author PVZN02821
 *
 */
public class HubicClient extends AbstractHTTPClient{


	private static final Logger LOGGER = LoggerFactory.getLogger( HubicClient.class );


	public InputStream telechargementFichier(String url){
		Invocation.Builder invocation = getInvocation(getClient(), url, null, MediaType.APPLICATION_OCTET_STREAM_TYPE);
		Response response = callHTTPGetData(invocation);
		// La réponse est un PDF
		if("application/pdf".equals(response.getMediaType().getType())){
			return response.readEntity(InputStream.class);
		}
		else{
			LOGGER.error("Erreur la réponse reçue de [{}] n'est pas un PDF", url);
			return null;
		}
	}
}
