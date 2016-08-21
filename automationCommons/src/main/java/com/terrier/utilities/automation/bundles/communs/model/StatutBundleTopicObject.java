/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.model;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.Bundle;

import com.terrier.utilities.automation.bundles.communs.enums.statut.StatutBundleEnum;

/**
 * Object de statut transmis dans le topic
 * @author vzwingma
 *
 */
public class StatutBundleTopicObject {

	// Bundle
	private Bundle bundle;
	
	private StatutBundleEnum statut;
	
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
	 * @return the statut
	 */
	public StatutBundleEnum getStatut() {
		return statut;
	}



	/**
	 * @param statut the statut to set
	 */
	public void setStatut(StatutBundleEnum statut) {
		this.statut = statut;
	}
	
	
}
