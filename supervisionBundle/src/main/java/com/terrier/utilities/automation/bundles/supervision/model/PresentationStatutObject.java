/**
 * 
 */
package com.terrier.utilities.automation.bundles.supervision.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.terrier.utilities.automation.bundles.supervision.communs.PresentationStatutModuleEnum;
import com.terrier.utilities.automation.bundles.supervision.communs.PresentationStatutPropertyEnum;

/**
 * Objet de présentation d'un statut
 * @author vzwingma
 *
 */
public class PresentationStatutObject {

	// Nom du groupe
	private String nomGroupe;
	// Nom du module
	private String nomModule;
	// Etat du module
	private PresentationStatutModuleEnum etatModule;
	// Date de mise à jour
	private Calendar dateMiseAJour;

	private List<PresentationStatutPropertyObject> listStatutPropertyObject = new ArrayList<PresentationStatutPropertyObject>();
	/**
	 * @return the nomGroupe
	 */
	public String getNomGroupe() {
		return nomGroupe;
	}

	/**
	 * @param nomGroupe the nomGroupe to set
	 */
	public void setNomGroupe(String nomGroupe) {
		this.nomGroupe = nomGroupe;
	}

	/**
	 * @return the nomModule
	 */
	public String getNomModule() {
		return nomModule;
	}

	/**
	 * @param nomModule the nomModule to set
	 */
	public void setNomModule(String nomModule) {
		this.nomModule = nomModule;
	}

	/**
	 * @return the etatModule
	 */
	public PresentationStatutModuleEnum getEtatModule() {
		return etatModule;
	}

	/**
	 * @param etatModule the etatModule to set
	 */
	public void setEtatModule(PresentationStatutModuleEnum etatModule) {
		this.etatModule = etatModule;
	}

	/**
	 * @return the dateMiseAJour
	 */
	public Calendar getDateMiseAJour() {
		return dateMiseAJour;
	}

	/**
	 * @param dateMiseAJour the dateMiseAJour to set
	 */
	public void setDateMiseAJour(Calendar dateMiseAJour) {
		this.dateMiseAJour = dateMiseAJour;
	}

	/**
	 * @return the listStatutPropertyObject
	 */
	public List<PresentationStatutPropertyObject> getListStatutPropertyObject() {
		return listStatutPropertyObject;
	}

	/**
	 * @return the statut du bundle
	 */
	public PresentationStatutPropertyEnum getStatutComponents() {

		PresentationStatutPropertyEnum statutBundle = PresentationStatutPropertyEnum.OK;
		if(!this.listStatutPropertyObject.isEmpty()){
			for (PresentationStatutPropertyObject statutPropertyBundleObject : listStatutPropertyObject) {
				if(statutPropertyBundleObject.getEtat().ordinal() > statutBundle.ordinal()){
					statutBundle = statutPropertyBundleObject.getEtat();
				}
			}
		}
		return statutBundle;
	}
}
