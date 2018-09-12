package com.terrier.utilities.automation.bundles.messaging.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enum des clés dans le fichier de configuration
 * @author vzwingma
 *
 */
public enum MessagingConfigKeyEnums {

	SEND_PERIODE_ENVOI("ALL", "automation.bundle.messaging.email.send.period"),
	EMAIL_KEY("EMAIL", "automation.bundle.messaging.email.key"),
	EMAIL_URL("EMAIL", "automation.bundle.messaging.email.mailgun.url"),
	EMAIL_DOMAIN("EMAIL", "automation.bundle.messaging.email.mailgun.domain"),
	EMAIL_SERVICE("EMAIL", "automation.bundle.messaging.email.mailgun.service"),
	EMAIL_DESTINATAIRES("EMAIL", "automation.bundle.messaging.email.destinataires"),

	SMS_USER("SMS", "automation.bundle.messaging.sms.user"),
	SMS_PASS("SMS", "automation.bundle.messaging.sms.pass"),
	SMS_URL( "SMS", "automation.bundle.messaging.sms.free.url"),

	SLACK_URL("SLACK", "automation.bundle.messaging.notification.slack.url"),
	SLACK_KEY("SLACK", "automation.bundle.messaging.notification.slack.key");
	
	// Code de la clé dans le fichier de configuration
	private String codeKey;
	private String type;

	/**
	 * Valeur de l'enum
	 * @param value
	 */
	private MessagingConfigKeyEnums(String type, String value){
		this.codeKey = value;
		this.type = type;
	}


	/**
	 * @return the codeKey
	 */
	public String getCodeKey() {
		return codeKey;
	}


	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	
	public static List<MessagingConfigKeyEnums> values(String type){
		return Arrays.asList(MessagingConfigKeyEnums.values()).stream().filter(m -> type.equals(m.getType())).collect(Collectors.toList());
	}
	
}
