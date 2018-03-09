/**
 * 
 */
package com.terrier.utilities.automation.bundles.supervision.osgi;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.supervision.business.BundleSupervisionBusinessService;
import com.terrier.utilities.automation.bundles.supervision.business.SupervisionServlet;

/**
 * Supervision Activator
 * @author vzwingma
 *
 */
@Singleton
public final class SupervisionActivator implements BundleActivator, ServiceTrackerCustomizer<HttpService, HttpService> {


	private static final Logger LOGGER = LoggerFactory.getLogger( SupervisionActivator.class );
	// Bundle Context
	private BundleContext bundleContext;
	// Tracker
	private ServiceTracker<HttpService, HttpService> tracker;

	@Inject BundleSupervisionBusinessService service;
	
	
	private static final String URL_STATUT_SUPERVISION = "/supervision/statut";

	/**
	 * Called when the OSGi framework starts our bundle
	 */
	public void start(BundleContext bc) throws Exception {
		bundleContext = bc;
		tracker = new ServiceTracker<>(bc,
				HttpService.class, this);
		tracker.open();
	}

	/**
	 * Called when the OSGi framework stops our bundle
	 */
	public void stop(BundleContext bc) throws Exception {
		tracker.close();
	}

	/* (non-Javadoc)
	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
	 */
	@Override
	public HttpService addingService(ServiceReference<HttpService> reference) {
		final HttpService httpService = bundleContext.getService(reference);
		if (httpService != null) {
			// create a default context to share between registrations
			final HttpContext httpContext = httpService
					.createDefaultHttpContext();
			// register the hello world servlet
			final Dictionary<String, Object> initParams = new Hashtable<>();
			initParams.put("from", "HttpService");
			try {
				httpService.registerServlet(URL_STATUT_SUPERVISION, // alias
						new SupervisionServlet(), // registered
						// servlet
						initParams, httpContext);
			} catch (ServletException | NamespaceException e) {
				LOGGER.error("Erreur lors de l'initialistion du service [{}]", URL_STATUT_SUPERVISION, e);
			}
		}
		return httpService;
	}

	/* (non-Javadoc)
	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference, java.lang.Object)
	 */
	@Override
	public void modifiedService(ServiceReference<HttpService> reference,
			HttpService service) {
		// ignore
	}

	/* (non-Javadoc)
	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference, java.lang.Object)
	 */
	@Override
	public void removedService(ServiceReference<HttpService> reference,
			HttpService service) {
		service.unregister(URL_STATUT_SUPERVISION);
	}
}