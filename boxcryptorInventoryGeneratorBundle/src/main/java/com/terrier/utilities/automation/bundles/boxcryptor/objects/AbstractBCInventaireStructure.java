package com.terrier.utilities.automation.bundles.boxcryptor.objects;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inventaire
 * @author vzwingma
 *
 */
public abstract class AbstractBCInventaireStructure {


	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBCInventaireStructure.class);

	private String nomFichierChiffre; 
	private String nomFichierClair;


	/**
	 * Constructeur YML
	 */
	public AbstractBCInventaireStructure(){ }
	
	public AbstractBCInventaireStructure(String nomFichierChiffre, String nomFichierClair){
		this.nomFichierChiffre = nomFichierChiffre;
		this.nomFichierClair = nomFichierClair;
	}

	/**
	 * @return la clé
	 */
	public String getCleMap(){
		return getCleMap(this.nomFichierClair);
	}
	
	/**
	 * @param fichierClair
	 * @return clé pour un répertoire
	 */
	public String getCleMap(String nomFichierClair){
		return getHashSHA256(nomFichierClair);
	}


	/**
	 * @param nameFileOrDirectory
	 * @return Hash SHA 256
	 */
	private String getHashSHA256(final String nameFileOrDirectory){
		try {
			if(nameFileOrDirectory != null){
				MessageDigest hash = MessageDigest.getInstance("SHA-256");
				hash.update(nameFileOrDirectory.getBytes(), 0, nameFileOrDirectory.length());
				return new BigInteger(1,hash.digest()).toString(16);
			}
			return null;

		} catch (final NoSuchAlgorithmException e) {
			LOGGER.error("Erreur lors du calcul du hash", e);
			return null;
		}
	}


	/**
	 * @return the nomFichierChiffre
	 */
	public String getNomFichierChiffre() {
		return nomFichierChiffre;
	}

	/**
	 * @param nomFichierChiffre the nomFichierChiffre to set
	 */
	public void setNomFichierChiffre(String nomFichierChiffre) {
		this.nomFichierChiffre = nomFichierChiffre;
	}

	/**
	 * @return the nomFichierClair
	 */
	public String getNomFichierClair() {
		return nomFichierClair;
	}

	/**
	 * @param nomFichierClair the nomFichierClair to set
	 */
	public void setNomFichierClair(String nomFichierClair) {
		this.nomFichierClair = nomFichierClair;
	}
}
