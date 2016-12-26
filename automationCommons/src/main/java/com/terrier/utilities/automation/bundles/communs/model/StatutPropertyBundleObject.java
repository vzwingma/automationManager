/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.model;

import com.terrier.utilities.automation.bundles.communs.enums.statut.StatutPropertyBundleEnum;

/**
 * Object de statut transmis dans le topic
 * @author vzwingma
 *
 */
public class StatutPropertyBundleObject {


	// Libellé de la propriété
	private String libelle;
	
	private StatutPropertyBundleEnum statut;
	
	// Valeur
	private Object value;

	
	
	
	/**
	 * @param libelle
	 * @param statut
	 * @param value
	 */
	public StatutPropertyBundleObject(String libelle, Object value, StatutPropertyBundleEnum statut) {
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
	public StatutPropertyBundleEnum getStatut() {
		return statut;
	}

	/**
	 * @param statut the statut to set
	 */
	public void setStatut(StatutPropertyBundleEnum statut) {
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("\n StatutPropertyBundleObject [libelle=").append(libelle).append(", statut=").append(statut)
				.append(", value=").append(value).append("]");
		return builder.toString();
	}
	
}
