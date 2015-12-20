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
}
