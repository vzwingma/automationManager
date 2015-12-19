/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.enums.messaging;

/**
 * @author vzwingma
 *
 */
public enum EventsTopicNameEnum {

	
	NOTIFIFY_MESSAGE("com/terrier/utilities/automation/message/notify"),
	SUPERVISION_EVENTS("com/terrier/utilities/automation/supervision/notify");

	// Topic name
	private String topicName;
	
	
	/**
	 * Valeur de l'enum
	 * @param value
	 */
	private EventsTopicNameEnum(String topicName){
		this.topicName = topicName;
	}


	/**
	 * @return the topicName
	 */
	public String getTopicName() {
		return topicName;
	}
}
