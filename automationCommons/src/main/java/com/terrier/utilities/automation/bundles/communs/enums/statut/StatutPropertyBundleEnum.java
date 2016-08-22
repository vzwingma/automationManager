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
public enum StatutPropertyBundleEnum implements IAutomationTopicPropertyNamesEnum {
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
	public static StatutPropertyBundleEnum getEnumFromName(String enumName){
		for (StatutPropertyBundleEnum enumStatus : StatutPropertyBundleEnum.values()) {
			if(enumStatus.getName().equals(enumName)){
				return enumStatus;
			}
		}
		return null;
	}
	
	// StaticFinal ordinal
	public static final int ORD_OK = 0;
	public static final int ORD_WARNING = 1;
	public static final int ORD_ERROR = 2;
}
