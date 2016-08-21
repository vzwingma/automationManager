/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.model;

import com.terrier.utilities.automation.bundles.communs.enums.statut.StatutBundleEnum;

/**
 * Object de statut transmis dans le topic
 * @author vzwingma
 *
 */
public class StatutPropertyBundleObject {


	// Libellé de la propriété
	private String libelle;
	
	private StatutBundleEnum statut;
	
	// Valeur
	private Object value;

	
	
	
	/**
	 * @param libelle
	 * @param statut
	 * @param value
	 */
	public StatutPropertyBundleObject(String libelle, Object value, StatutBundleEnum statut) {
		super();
		this.libelle = libelle;
		this.statut = statut;
		this.value = value;
	}

	/**
	 * @return the libelle
	 */
	public String getLibelle() {
		return libelle;
	}

	/**
	 * @param libelle the libelle to set
	 */
	public void setLibelle(String libelle) {
		this.libelle = libelle;
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

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	
}
