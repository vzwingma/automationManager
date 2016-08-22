/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.model;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.Bundle;

import com.terrier.utilities.automation.bundles.communs.enums.statut.StatutPropertyBundleEnum;

/**
 * Object de statut transmis dans le topic
 * @author vzwingma
 *
 */
public class StatutBundleTopicObject {

	// Bundle
	private Bundle bundle;
	
	// Properties
	private List<StatutPropertyBundleObject> properties = new ArrayList<StatutPropertyBundleObject>();
	
	/**
	 * Statut du Bundle
	 * @param bundle
	 */
	public StatutBundleTopicObject(Bundle bundle){
		this.bundle = bundle;
	}

	
	
	/**
	 * @param bundle the bundle to set
	 */
	public void setBundle(Bundle bundle) {
		this.bundle = bundle;
	}



	/**
	 * @return the bundle
	 */
	public Bundle getBundle() {
		return bundle;
	}


	/**
	 * @return the properties
	 */
	public List<StatutPropertyBundleObject> getProperties() {
		return properties;
	}



	/**
	 * @return the statut du bundle
	 */
	public StatutPropertyBundleEnum getStatut() {
		
		StatutPropertyBundleEnum statutBundle = StatutPropertyBundleEnum.OK;
		if(!this.properties.isEmpty()){
			for (StatutPropertyBundleObject statutPropertyBundleObject : properties) {
				if(statutPropertyBundleObject.getStatut().ordinal() > statutBundle.ordinal()){
					statutBundle = statutPropertyBundleObject.getStatut();
				}
			}
		}
		return statutBundle;
	}
}
