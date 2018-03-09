/**
 * 
 */
package com.terrier.utilities.automation.bundles.supervision.model;

import java.util.ArrayList;
import java.util.List;

import com.terrier.utilities.automation.bundles.communs.model.StatutBundleTopicObject;
import com.terrier.utilities.automation.bundles.communs.model.StatutPropertyBundleObject;
import com.terrier.utilities.automation.bundles.supervision.communs.OSGIStatusUtils;
import com.terrier.utilities.automation.bundles.supervision.communs.PresentationStatutPropertyEnum;

/**
 * 
 * Translator 
 * @author vzwingma
 *
 */
public class PresentationStatutObjectTranslator {

	// Constructeur privé
	private PresentationStatutObjectTranslator(){}
	/**
	 * @param bundleObject
	 * @return translation en objet de présentation
	 */
	public static PresentationStatutObject translateFrom(StatutBundleTopicObject bundleObject){
		PresentationStatutObject presentation = new PresentationStatutObject();
		presentation.setNomGroupe("AutomationManager");

		StringBuilder titre = new StringBuilder();
		titre.append("[").append(bundleObject.getBundle().getBundleId()).append("] ").append(bundleObject.getBundle().getSymbolicName()).append(" ").append(bundleObject.getBundle().getVersion());
		presentation.setNomModule(titre.toString());

		presentation.setDateMiseAJour(bundleObject.getMiseAJour());

		presentation.setEtatModule(OSGIStatusUtils.getBundleStatusLibelle(bundleObject.getBundle().getState()));

		presentation.getListStatutPropertyObject().addAll(translateFrom(bundleObject.getProperties()));

		return presentation;
	}


	/**
	 * @param propertyObjects
	 * @return liste des properties en mode présentation
	 */
	public static List<PresentationStatutPropertyObject> translateFrom(List<StatutPropertyBundleObject> propertyObjects){
		List<PresentationStatutPropertyObject> properties = new ArrayList<>();
		if(propertyObjects != null && !propertyObjects.isEmpty() ){
			for (StatutPropertyBundleObject propertyObject : propertyObjects) {
				properties.add(translateFrom(propertyObject));
			}
		}
		return properties;

	}


	/**
	 * @param propertyObject
	 * @return properties en mode présentation
	 */
	public static PresentationStatutPropertyObject translateFrom(StatutPropertyBundleObject propertyObject){
		PresentationStatutPropertyObject property = new PresentationStatutPropertyObject();
		property.setNom(propertyObject.getLibelle());
		property.setValeur(propertyObject.getValue());
		property.setEtat(PresentationStatutPropertyEnum.valueOf(propertyObject.getStatut().getName()));
		return property;
	}

}
