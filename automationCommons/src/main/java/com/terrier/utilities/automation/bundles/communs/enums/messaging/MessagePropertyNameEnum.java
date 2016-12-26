/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.enums.messaging;

import com.terrier.utilities.automation.bundles.communs.enums.IAutomationTopicPropertyNamesEnum;

/**
 * Nom des propriétés de l'event vers Type de messages
 * @author vzwingma
 *
 */
public enum MessagePropertyNameEnum implements IAutomationTopicPropertyNamesEnum {
	MESSAGE,
	TIME,
	TITRE_MESSAGE,
	TYPE_MESSAGE
	;

	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.communs.enums.messaging.AutomationTopicPropertyNamesEnum#getName()
	 */
	@Override
	public String getName() {
		return this.name();
	}
}
