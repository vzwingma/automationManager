package com.terrier.utilities.automation.bundles.communs.utils.files.visitors;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

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
	private Calendar dateDernier;
	/**
	 * Copy
	 * @param fromPath répertoire source
	 * @param toPath répertoire cible
	 */
	public CopyDirVisitor(Path fromPath, Path toPath, AtomicInteger nbFichiersCopies, Calendar dateDernier){
		this.fromPath = fromPath;
		this.toPath = toPath;
		this.nbFichiersCopies = nbFichiersCopies;
		this.dateDernier = dateDernier;
	}


	/* (non-Javadoc)
	 * @see java.nio.file.SimpleFileVisitor#preVisitDirectory(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
	 */
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		Path targetPath = toPath.resolve(fromPath.relativize(dir));
		if(!Files.exists(targetPath)){
			this.nbFichiersCopies.incrementAndGet();
			Files.createDirectory(targetPath);
		}
		return FileVisitResult.CONTINUE;
	}

	/* (non-Javadoc)
	 * @see java.nio.file.SimpleFileVisitor#visitFile(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
	 */
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		if(this.dateDernier == null || Files.getLastModifiedTime(file).toMillis() > this.dateDernier.getTimeInMillis()){
			Files.copy(file, toPath.resolve(fromPath.relativize(file)), copyOption);
			this.nbFichiersCopies.incrementAndGet();
		}
		return FileVisitResult.CONTINUE;
	}
}