/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.osgi.framework.Bundle;

/**
 * Object de statut transmis dans le topic
 * @author vzwingma
 *
 */
public class StatutBundleTopicObject {

	// Bundle
	private Bundle bundle;
	// Properties
	private List<StatutPropertyBundleObject> properties = new ArrayList<>();
	// Timestamp
	private Calendar miseAJour = Calendar.getInstance();
	
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
	 * @return the miseAJour
	 */
	public Calendar getMiseAJour() {
		return miseAJour;
	}
}
