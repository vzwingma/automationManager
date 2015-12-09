/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs;

/**
 * @author vzwingma
 *
 */
public enum EventsTopicName {

	
	NOTIFIFY_MESSAGE("com/terrier/utilities/automation/message/notify");

	// Topic name
	private String topicName;
	
	
	/**
	 * Valeur de l'enum
	 * @param value
	 */
	private EventsTopicName(String topicName){
		this.topicName = topicName;
	}


	/**
	 * @return the topicName
	 */
	public String getTopicName() {
		return topicName;
	}
}
