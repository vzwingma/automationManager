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
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.terrier.utilities.automation.bundles.communs.enums.messaging.StatusPropertyNameEnum;


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
		writer.println("<p align='left'>");
	}


	/**
	 * Page de statut
	 * @param writer
	 */
	private void statutPage(final PrintWriter writer){
		Map<String, Map<StatusPropertyNameEnum, Object>> supervision = SupervisionBusinessService.getStatutBundles();
		
		for (Entry<String, Map<StatusPropertyNameEnum, Object>> bundleStatut : supervision.entrySet()) {
			writer.println("<h2>" + bundleStatut.getKey() + "</h2");
			writer.println("<ul>");
			for (Entry<StatusPropertyNameEnum, Object> bundleValue : bundleStatut.getValue().entrySet()) {
				writer.println("<li>" + bundleValue.getKey() + " : " + bundleValue.getValue() + "</li>");	
			}
			writer.println("</ul>");
			
		}
		
	}
	
	/**
	 * Footer
	 * @param writer
	 */
	private void footer(final PrintWriter writer){
		writer.println("<br/>");
		writer.println("</p>");
		writer.println("</body></html>");
	}
}