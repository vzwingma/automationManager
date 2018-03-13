/**
 * 
 */
package com.terrier.utilities.automation.bundles.emails.worker.business.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;

/**
 * A helper class for Google's Gmail API.
 * A partir de  https://developers.google.com/gmail/api/quickstart/java
 *
 */
public final class GoogleAuthHelper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger( GoogleAuthHelper.class );
	
	
	   /** Application name. */
    private static final String APPLICATION_NAME =
        "Gmail API for Automation";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File("src/main/resources/credentials/gmail");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory datastoreFactory;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
        JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport httpTransport;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/*
     */
    private static List<String> scopesAPI;

    static {
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            datastoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Exception t) {
            LOGGER.error("Erreur lors de l'initialisation ", t);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
        		GoogleAuthHelper.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets =
            GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        httpTransport, JSON_FACTORY, clientSecrets, scopesAPI)
                .setDataStoreFactory(datastoreFactory)
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(
            flow, new LocalServerReceiver()).authorize("user");
        LOGGER.info("Les credentials sont enregistrés ici : {}", DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Gmail client service.
     * @return an authorized Gmail client service
     * @throws IOException
     */
    public static Gmail getGmailService(String scope) throws IOException {
    	
    	scopesAPI = Arrays.asList(scope);
    	
        Credential credential = authorize();
        return new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Executer pour peupler les credentials
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // Build a new authorized API client service.
        Gmail service = getGmailService(GmailScopes.GMAIL_LABELS);

        // Print the labels in the user's account.
        String user = "me";
        ListLabelsResponse listResponse = service.users().labels().list(user).execute();
        List<Label> labels = listResponse.getLabels();
        if (labels.size() == 0) {
           LOGGER.warn("No labels found.");
        } else {
            LOGGER.info("Labels:");
            for (Label label : labels) {
                LOGGER.info("- {}", label);
            }
        }
    }
}