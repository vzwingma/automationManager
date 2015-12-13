package com.terrier.utilities.automation.bundles.boxcryptor.generate.objects;

/**
 * Représente un couple fichier clair/chiffré
 * @author vzwingma
 *
 */
public class BCInventaireFichier extends AbstractBCInventaireStructure {

	/**
	 * Constructeur YML
	 */
	public BCInventaireFichier(){}
	
	/**
	 * Constructeur
	 * @param nomFichierChiffre fichier chiffré
	 * @param nomFichierClair fichier en clair
	 */
	public BCInventaireFichier(String nomFichierChiffre, String nomFichierClair) {
		super(nomFichierChiffre, nomFichierClair);
	}
}
