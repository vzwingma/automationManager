/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.enums.statut;

import com.terrier.utilities.automation.bundles.communs.enums.IAutomationTopicPropertyNamesEnum;

/**
 * Nom des propriétés de l'event vers Type de messages
 * @author vzwingma
 *
 */
public enum StatutBundleEnum implements IAutomationTopicPropertyNamesEnum {
	OK,
	WARNING,
	ERROR
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
	public static StatutBundleEnum getEnumFromName(String enumName){
		for (StatutBundleEnum enumStatus : StatutBundleEnum.values()) {
			if(enumStatus.getName().equals(enumName)){
				return enumStatus;
			}
		}
		return null;
	}
}
