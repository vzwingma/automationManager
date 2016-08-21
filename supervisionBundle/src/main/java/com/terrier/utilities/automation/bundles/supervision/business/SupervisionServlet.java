/**
 * 
 */
package com.terrier.utilities.automation.bundles.supervision.business;

/**
 * @author vzwingma
 *
 */
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.terrier.utilities.automation.bundles.communs.model.StatutBundleTopicObject;
import com.terrier.utilities.automation.bundles.communs.model.StatutPropertyBundleObject;
import com.terrier.utilities.automation.bundles.supervision.communs.OSGIStatusUtils;


/**
 * Page de la supervision
 * @author vzwingma
 *
 */
public class SupervisionServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7218593433411480431L;

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
	IOException {
		response.setContentType("text/html");

		final PrintWriter writer = response.getWriter();
		// Création de l'entête
		header(writer);

		statutPage(writer);

		// Création du footer		
		footer(writer);
	}

	/**
	 * Entête
	 * @param writer
	 */
	private void header(final PrintWriter writer){
		writer.println("<html><body align='center'>");
		writer.println("<h1>Supervision des bundles de l'AutomationManager</h1>");
	}


	/**
	 * Page de statut
	 * @param writer
	 */
	private void statutPage(final PrintWriter writer){
		Map<Long, StatutBundleTopicObject> supervision = SupervisionBusinessService.getStatutBundles();
		writer.println("<p align='left'>");
		for (StatutBundleTopicObject bundleStatut : supervision.values()) {
			writer.println("<h2> [" + bundleStatut.getBundle().getBundleId() + "] " + bundleStatut.getBundle().getSymbolicName() + "</h2");
			writer.println("<br> [" + OSGIStatusUtils.getBundleStatusLibelle(bundleStatut.getBundle().getState()) + "] [" + bundleStatut.getStatut() + "]"); 
			writer.println("<ul>");
			for (StatutPropertyBundleObject bundleValue : bundleStatut.getProperties()) {
				writer.println("<li>" + bundleValue.getLibelle() + " : " + bundleValue.getValue() + "</li>");	
			}
			writer.println("</ul>");
		}
		writer.println("</p>");
	}

	/**
	 * Footer
	 * @param writer
	 */
	private void footer(final PrintWriter writer){
		writer.println("<br/>");

		writer.println("</body></html>");
	}
}