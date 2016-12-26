/**
 * 
 */
package com.terrier.utilities.automation.bundles.save.to.business.runnable;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.save.to.business.enums.CommandeEnum;

/**
 * Test de la tâche Runnable
 * @author vzwingma
 *
 */
public class TestSaveToTaskRunnable {


	private static final Logger LOGGER = LoggerFactory.getLogger( TestSaveToTaskRunnable.class );
	
	private static final String CHEMIN_FICHIER="src/test/resources/download/_HUBICEU257005.pdf";
	
	/**
	 * @throws IOException
	 */
	@BeforeClass
	public static void initFiles() throws IOException{
		LOGGER.info("Création des fichiers init dans {} : {}", FileSystems.getDefault().getPath("src/test/resources/download/").toAbsolutePath(), Files.isDirectory(FileSystems.getDefault().getPath("src/test/resources/download/")));
		if(!Files.exists(FileSystems.getDefault().getPath(CHEMIN_FICHIER))){
			Files.createFile(FileSystems.getDefault().getPath(CHEMIN_FICHIER));
		}
	}


	/**
	 * Test de copie
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	@Test
	public void testCopieFile() throws IOException, InterruptedException{
		SaveToTaskRunnable spyTask = spy(new SaveToTaskRunnable(0, 
				CommandeEnum.COPY, 
				"src/test/resources/download", 
				"_HUBIC[A-Za-z0-9_]*.pdf", 
				"src/test/resources/bc", 
				null,
				null));

		Mockito.doNothing().when(spyTask).sendNotificationMessage(anyString());
		when(spyTask.copyFichierTo(any(Path.class), eq("_HUBICEU257005.pdf"), anyString())).thenReturn(Boolean.TRUE);
		when(spyTask.getDateInitScan()).thenReturn(null);
		// Premier traitement, la copie est réalisée
		spyTask.run();

		assertNotNull(spyTask.getDateDernierScan());
		verify(spyTask, times(1)).copyFichierTo(any(Path.class), eq("_HUBICEU257005.pdf"), anyString());

		// 2nd traitement, la copie n'est pas réalisée (toujours un seul appel)
		spyTask.run();
		verify(spyTask, times(1)).copyFichierTo(any(Path.class), eq("_HUBICEU257005.pdf"), anyString());

		// 3nd traitement, la copie est réalisée car changement
		Files.delete(FileSystems.getDefault().getPath(CHEMIN_FICHIER));
		Thread.sleep(2000);
		Files.createFile(FileSystems.getDefault().getPath(CHEMIN_FICHIER));

		spyTask.run();
		verify(spyTask, times(2)).copyFichierTo(any(Path.class), eq("_HUBICEU257005.pdf"), anyString());
	}
	
	
	


	/**
	 * Test de copie
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	@Test
	public void testCopieRepertoire() throws IOException, InterruptedException{
		SaveToTaskRunnable spyTask = spy(new SaveToTaskRunnable(0, 
				CommandeEnum.COPY, 
				"src/test/resources/download/directory", 
				null, 
				"src/test/resources/bc", 
				null, null));

		Mockito.doNothing().when(spyTask).sendNotificationMessage(anyString());
		when(spyTask.copyDirTo(any(Path.class), anyString(), anyListOf(String.class))).thenCallRealMethod();
		when(spyTask.getDateInitScan()).thenReturn(null);

		LOGGER.info("*** 1er traitement ***");
		// Premier traitement, la copie est réalisée
		spyTask.run();

		assertNotNull(spyTask.getDateDernierScan());
		verify(spyTask, times(1)).sendNotificationMessage(anyString(),eq("src/test/resources/download/directory"),anyString(),anyString(),anyString(),anyString());

		// 2nd traitement, la copie n'est pas réalisée (toujours un seul appel)
		LOGGER.info("*** 2ème traitement ***");
		spyTask.run();
		// Toujours qu'un seul appel
		verify(spyTask, times(1)).sendNotificationMessage(anyString(),eq("src/test/resources/download/directory"),anyString(),anyString(),anyString(),anyString());
		
		// 3nd traitement, la copie est réalisée car changement
		LOGGER.info("*** 3ème traitement ***");
		Files.delete(FileSystems.getDefault().getPath("src/test/resources/download/directory/d1.txt"));
		Thread.sleep(2000);
		Files.createFile(FileSystems.getDefault().getPath("src/test/resources/download/directory/d1.txt"));

		spyTask.run();
		verify(spyTask, times(1)).sendNotificationMessage(anyString(),eq("src/test/resources/download/directory"),anyString(),eq("1"), anyString(),eq(""));
		
		// 4nd traitement, la copie est réalisée car changement mais erreur lors de la copie
		Files.delete(FileSystems.getDefault().getPath("src/test/resources/download/directory/d1.txt"));
		Thread.sleep(2000);
		
		Files.createFile(FileSystems.getDefault().getPath("src/test/resources/download/directory/d1.txt"));
		Files.delete(FileSystems.getDefault().getPath("src/test/resources/bc/subdirectory/d2.txt"));
		Files.delete(FileSystems.getDefault().getPath("src/test/resources/bc/subdirectory"));
	}
	
	
	@Ignore
	public void testReel(){
		SaveToTaskRunnable copyTask = new SaveToTaskRunnable(0, 
				CommandeEnum.MOVE, 
				"D:/Profiles/vzwingma/Downloads", 
				"[A-Za-z]*.postman_collection.json", 
				"X:/encrypted-sync/Postman", 
				null, null);
		
		copyTask.run();
	}

}
