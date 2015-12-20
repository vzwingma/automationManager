/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.enums.messaging;

/**
 * Nom des propriétés de l'event vers Type de messages
 * @author vzwingma
 *
 */
public enum StatusPropertyNameEnum implements AutomationTopicPropertyNamesEnum {
	STATUS,
	TIME,
	BUNDLE
	;

	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.communs.enums.messaging.AutomationTopicPropertyNamesEnum#getName()
	 */
	@Override
	public String getName() {
		return this.name();
	}
	
	
	/**
	 * @param enumName nom de l'enum
	 * @return l'enum correspondant
	 */
	public static StatusPropertyNameEnum getEnumFromName(String enumName){
		for (StatusPropertyNameEnum enumStatus : StatusPropertyNameEnum.values()) {
			if(enumStatus.getName().equals(enumName)){
				return enumStatus;
			}
		}
		return null;
	}
}
