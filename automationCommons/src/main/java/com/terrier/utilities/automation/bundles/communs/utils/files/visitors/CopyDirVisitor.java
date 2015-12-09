package com.terrier.utilities.automation.bundles.communs.utils.files.visitors;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Visitor permettant de copier l'ensemble d'un répertoire dans un autre
 * @author vzwingma
 *
 */
public class CopyDirVisitor extends SimpleFileVisitor<Path> {
    private Path fromPath;
    private Path toPath;
    private StandardCopyOption copyOption = StandardCopyOption.REPLACE_EXISTING;
    
    /**
     * Copy
     * @param fromPath répertoire source
     * @param toPath répertoire cible
     */
    public CopyDirVisitor(Path fromPath, Path toPath){
    	this.fromPath = fromPath;
    	this.toPath = toPath;
    }
    
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        Path targetPath = toPath.resolve(fromPath.relativize(dir));
        if(!Files.exists(targetPath)){
            Files.createDirectory(targetPath);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.copy(file, toPath.resolve(fromPath.relativize(file)), copyOption);
        return FileVisitResult.CONTINUE;
    }
}