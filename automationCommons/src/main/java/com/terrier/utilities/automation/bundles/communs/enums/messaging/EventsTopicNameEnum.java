/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.enums.messaging;

import com.terrier.utilities.automation.bundles.communs.enums.statut.StatutPropertyNameEnum;

/**
 * @author vzwingma
 *
 */
public enum EventsTopicNameEnum {

	
	NOTIFIFY_MESSAGE("com/terrier/utilities/automation/message/notify", MessagePropertyNameEnum.class),
	SUPERVISION_EVENTS("com/terrier/utilities/automation/supervision/notify", StatutPropertyNameEnum.class);

	// Topic name
	private String topicName;
	
	private Class<?> enumPropertyName;
	/**
	 * Valeur de l'enum
	 * @param value
	 */
	private EventsTopicNameEnum(String topicName, Class<?> enumPropertyName){
		this.topicName = topicName;
		this.enumPropertyName = enumPropertyName;
	}


	/**
	 * @return the topicName
	 */
	public String getTopicName() {
		return topicName;
	}


	/**
	 * @return the enumPropertyName
	 */
	public Class<?> getEnumPropertyName() {
		return enumPropertyName;
	}
	
	
	
}
