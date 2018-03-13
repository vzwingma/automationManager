package com.terrier.utilities.automation.bundles.communs.utils.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe utilitaire des fichiers
 * @author PVZN02821
 *
 */
public class FileUtils {


	private static final Logger LOGGER = LoggerFactory.getLogger( FileUtils.class );
	
	// Constructeur privé
	private FileUtils(){}
	/**
	 * Enregistrement d'un flux en local
	 * @param stream
	 * @param nomFichier
	 */
	public static void saveStreamToFile(InputStream stream, String nomFichier) {
		File targetFile = new File(nomFichier);
		try(InputStream iStream = stream){
			try(OutputStream out = new FileOutputStream(targetFile)){
				byte buf[]=new byte[1024];
				int len;
				while((len=stream.read(buf))>0){
					out.write(buf,0,len);
				}
			}
			catch (Exception e) {
				LOGGER.error("Erreur lors de l'enregistrement du flux", e);
			}
		}
		catch(Exception e){
			LOGGER.error("Erreur lors de l'enregistrement du flux", e);
		}
	}
}
