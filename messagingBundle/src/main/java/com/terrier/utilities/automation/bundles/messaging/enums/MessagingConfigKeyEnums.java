package com.terrier.utilities.automation.bundles.messaging.enums;

/**
 * Enum des clés dans le fichier de configuration
 * @author vzwingma
 *
 */
public enum MessagingConfigKeyEnums {

	SEND_PERIODE_ENVOI("automation.bundle.messaging.email.send.period"),
	EMAIL_KEY("automation.bundle.messaging.email.key"),
	EMAIL_URL("automation.bundle.messaging.email.mailgun.url"),
	EMAIL_DOMAIN("automation.bundle.messaging.email.mailgun.domain"),
	EMAIL_SERVICE("automation.bundle.messaging.email.mailgun.service"),
	EMAIL_DESTINATAIRES("automation.bundle.messaging.email.destinataires"),

	SMS_USER("automation.bundle.messaging.sms.user"),
	SMS_PASS("automation.bundle.messaging.sms.pass"),
	SMS_URL("automation.bundle.messaging.sms.free.url"),

	SLACK_URL("automation.bundle.messaging.notification.slack.url"),
	SLACK_KEY("automation.bundle.messaging.notification.slack.key");
	
	// Code de la clé dans le fichier de configuration
	private String codeKey;


	/**
	 * Valeur de l'enum
	 * @param value
	 */
	private MessagingConfigKeyEnums(String value){
		this.codeKey = value;
	}


	/**
	 * @return the codeKey
	 */
	public String getCodeKey() {
		return codeKey;
	}
}
