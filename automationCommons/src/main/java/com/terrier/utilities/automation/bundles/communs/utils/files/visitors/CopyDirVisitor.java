package com.terrier.utilities.automation.bundles.communs.utils.files.visitors;

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Visitor permettant de copier l'ensemble d'un répertoire dans un autre
 * @author vzwingma
 *
 */
public class CopyDirVisitor extends SimpleFileVisitor<Path> {
	private Path fromPath;
	private Path toPath;
	private StandardCopyOption copyOption = StandardCopyOption.REPLACE_EXISTING;
	private AtomicInteger nbFichiersCopies;
	private List<String> fichierErreur;
	private Calendar dateDernier;


	private static final Logger LOGGER = LoggerFactory.getLogger( CopyDirVisitor.class );
	/**
	 * Copy
	 * @param fromPath répertoire source
	 * @param toPath répertoire cible
	 */
	public CopyDirVisitor(Path fromPath, Path toPath, AtomicInteger nbFichiersCopies, List<String> fichierErreur, Calendar dateDernier){
		this.fromPath = fromPath;
		this.toPath = toPath;
		this.nbFichiersCopies = nbFichiersCopies;
		this.dateDernier = dateDernier;
		this.fichierErreur = fichierErreur;
	}


	/* (non-Javadoc)
	 * @see java.nio.file.SimpleFileVisitor#preVisitDirectory(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
	 */
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
		Path targetPath = toPath.resolve(fromPath.relativize(dir));
		if(!Files.exists(targetPath)){
			try{
				this.nbFichiersCopies.incrementAndGet();
				Files.createDirectory(targetPath);
			}
			catch(Exception e){
				StringBuilder b = new StringBuilder("répertoire [").append(targetPath).append("]");
				LOGGER.error("Erreur lors de la copie du {}", b.toString());
				this.fichierErreur.add(b.toString());
			}
		}
		return FileVisitResult.CONTINUE;
	}

	/* (non-Javadoc)
	 * @see java.nio.file.SimpleFileVisitor#visitFile(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
	 */
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
		try{
			if(this.dateDernier == null || Files.getLastModifiedTime(file).toMillis() > this.dateDernier.getTimeInMillis()){
				Files.copy(file, toPath.resolve(fromPath.relativize(file)), copyOption);
				this.nbFichiersCopies.incrementAndGet();
			}
		}
		catch (Exception e) {
			StringBuilder b = new StringBuilder("fichier [").append(file).append("]");
			LOGGER.error("Erreur lors de la copie du {}", b.toString());
			this.fichierErreur.add(b.toString());
		}
		return FileVisitResult.CONTINUE;

	}
}