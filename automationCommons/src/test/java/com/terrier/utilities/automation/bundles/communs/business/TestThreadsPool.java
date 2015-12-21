/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.business;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gestion du pool de thread
 * @author vzwingma
 *
 */
public class TestThreadsPool {


	private final Logger LOGGER = LoggerFactory.getLogger( TestThreadsPool.class );

	/**
	 *  Pool threads
	 */
	private ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(5);

	private int nbExecution = 0;

	@Test
	public void testThreadPoolUsage() throws InterruptedException{

		// Init
		assertEquals(5, scheduledThreadPool.getCorePoolSize());
		assertEquals(0, scheduledThreadPool.getPoolSize());

		ScheduledFuture<?> f = scheduledThreadPool.scheduleAtFixedRate(new DummyTestRunnable(), 0, 1, TimeUnit.SECONDS);
		LOGGER.debug("PendingTasks : {}", this.scheduledThreadPool.getQueue().size());
		Thread.sleep(9500);
		LOGGER.debug("PendingTasks : {}", this.scheduledThreadPool.getQueue().size());
		LOGGER.debug("** Fin **");
		logPool();

		assertEquals(10, this.nbExecution);
		// 10 s
		assertEquals(5, scheduledThreadPool.getCorePoolSize());

		LOGGER.debug("** CANCEL **");
		LOGGER.debug("PendingTasks : {}", this.scheduledThreadPool.getQueue().size());
		f.cancel(true);
		Thread.sleep(1000);
		LOGGER.debug("PendingTasks : {}", this.scheduledThreadPool.getQueue().size());
		// 10 s
		assertEquals(5, scheduledThreadPool.getCorePoolSize());
		assertEquals(0, scheduledThreadPool.getActiveCount());

		f = scheduledThreadPool.scheduleAtFixedRate(new DummyTestRunnable(), 0, 1, TimeUnit.SECONDS);
		LOGGER.debug("PendingTasks : {}", this.scheduledThreadPool.getQueue().size());
		Thread.sleep(9500);
		LOGGER.debug("PendingTasks : {}", this.scheduledThreadPool.getQueue().size());
		LOGGER.debug("** Fin2 **");
		assertEquals(20, this.nbExecution);
		logPool();
	}




	/**
	 * 
	 */
	private void logPool(){
//		LOGGER.debug("ScheduledThreadPool : ActiveCount : {}, PoolSize : {}, CorePoolSize : {}, CompletedTasks : {}", 
//				scheduledThreadPool.getActiveCount(), 
//				scheduledThreadPool.getPoolSize(), 
//				scheduledThreadPool.getCorePoolSize(), 
//				scheduledThreadPool.getCompletedTaskCount()
//				);
	}


	/**
	 * @author vzwingma
	 *
	 */
	public class DummyTestRunnable implements Runnable{
		@Override
		public void run() {
			nbExecution ++;
			LOGGER.debug("   {} runs of DummyTestRunnable", nbExecution);
			logPool();
		}
	}
}
