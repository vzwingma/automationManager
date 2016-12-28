/**
 * 
 */
package com.terrier.utilities.automation.bundles.supervision.model;

import com.terrier.utilities.automation.bundles.supervision.communs.PresentationStatutPropertyEnum;

/**
 * Objet de présentation d'un statut
 * @author vzwingma
 *
 */
public class PresentationStatutPropertyObject {

	// Nom de la propriété
	private String nom;
	// Etat du module
	private PresentationStatutPropertyEnum etat;
	// Valeur
	private Object valeur;
	/**
	 * @return the nom
	 */
	public String getNom() {
		return nom;
	}
	/**
	 * @param nom the nom to set
	 */
	public void setNom(String nom) {
		this.nom = nom;
	}
	/**
	 * @return the etat
	 */
	public PresentationStatutPropertyEnum getEtat() {
		return etat;
	}
	/**
	 * @param etat the etat to set
	 */
	public void setEtat(PresentationStatutPropertyEnum etat) {
		this.etat = etat;
	}
	/**
	 * @return the valeur
	 */
	public Object getValeur() {
		return valeur;
	}
	/**
	 * @param valeur the valeur to set
	 */
	public void setValeur(Object valeur) {
		this.valeur = valeur;
	}

}
