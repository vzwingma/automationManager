package com.terrier.utilities.automation.bundles.supervision.communs;

import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceEvent;

public class OSGIStatusUtils {

	
	
	/**
	 * @param status
	 * @return le libelle du status du service
	 */
	public static String getServiceStatusLibelle(int status){
		
		switch (status) {
		case ServiceEvent.REGISTERED:
			return "REGISTERED";
		case ServiceEvent.UNREGISTERING:
			return "UNREGISTERING";
		case ServiceEvent.MODIFIED:
			return "MODIFIED";
		case ServiceEvent.MODIFIED_ENDMATCH:
			return "MODIFIED ENDMATCH";
		default:
			return null;
		}
	}
	
	/**
	 * @param status statut
	 * @return le libelle du status du service
	 */
	public static String getBundleStatusLibelle(int status){
		
		switch (status) {
		case BundleEvent.INSTALLED:
			return "INSTALLED";
		case BundleEvent.LAZY_ACTIVATION:
			return "LAZY ACTIVATION";
		case BundleEvent.RESOLVED:
			return "RESOLVED";
		case BundleEvent.STARTED:
			return "STARTED";
		case BundleEvent.STARTING:
			return "STARTING";
		case BundleEvent.STOPPED:
			return "STOPPED";
		case BundleEvent.STOPPING:
			return "STOPPING";
		case BundleEvent.UNINSTALLED:
			return "UNINSTALLED";
		case BundleEvent.UNRESOLVED:
			return "UNRESOLVED";
		case BundleEvent.UPDATED:
			return "UPDATED";
		default:
			return null;
		}
	}
}
