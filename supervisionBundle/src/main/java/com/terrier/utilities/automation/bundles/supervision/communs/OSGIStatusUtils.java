package com.terrier.utilities.automation.bundles.supervision.communs;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceEvent;

import com.terrier.utilities.automation.bundles.communs.enums.statut.StatutPropertyBundleEnum;

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
	public static String getBundleStatusEventLibelle(int status){
		
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
	
	
	/**
	 * @param status statut
	 * @return le libelle du status du service
	 */
	public static String getBundleStatusLibelle(int status){
		
		switch (status) {
		case Bundle.INSTALLED:
			return "INSTALLED";
		case Bundle.RESOLVED:
			return "STOPPED";
		case Bundle.ACTIVE:
			return "STARTED";
		case Bundle.STARTING:
			return "STARTING";
		case Bundle.STOPPING:
			return "STOPPING";
		case Bundle.UNINSTALLED:
			return "UNINSTALLED";
		default:
			return null;
		}
	}
	/**
	 * @param status statut
	 * @return le libelle du status du service
	 */
	public static String getBundleStatusStyleColor(int status){
		
		switch (status) {
		case Bundle.INSTALLED:
			return "grey";
		case Bundle.RESOLVED:
			return "red";
		case Bundle.ACTIVE:
			return "green";
		case Bundle.STARTING:
			return "orange";
		case Bundle.STOPPING:
			return "orange";
		case Bundle.UNINSTALLED:
			return "grey";
		default:
			return null;
		}
	}
	
	


	/**
	 * @param status statut
	 * @return le libelle du status du service
	 */
	public static String getBundleStatusStyleColor(StatutPropertyBundleEnum status){
		
		switch (status.ordinal()) {
		case StatutPropertyBundleEnum.ORD_OK:
			return "green";
		case StatutPropertyBundleEnum.ORD_WARNING:
			return "orange";
		case StatutPropertyBundleEnum.ORD_ERROR:
			return "red";
		default:
			return null;
		}
	}
}
