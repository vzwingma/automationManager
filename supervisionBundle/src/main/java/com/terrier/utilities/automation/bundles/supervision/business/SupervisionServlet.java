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
import java.text.SimpleDateFormat;
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
	
	private static final SimpleDateFormat DATE_MAJ_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
	IOException {
		response.setContentType("text/html");

		final PrintWriter writerPage = response.getWriter();
		final StringBuilder writer = new StringBuilder();
		// Création de l'entête
		header(writer);

		statutPage(writer);

		// Création du footer		
		footer(writer);
		
		writerPage.println(writer.toString());
	}

	/**
	 * Entête
	 * @param writer
	 */
	private void header(final StringBuilder writer){
		writer.append("<html><body align='center'>");
		writer.append("<h1>Supervision des bundles de l'AutomationManager</h1>");
	}


	/**
	 * Page de statut
	 * @param writer
	 */
	private void statutPage(final StringBuilder writer){
		Map<Long, StatutBundleTopicObject> supervision = SupervisionBusinessService.getStatutBundles();
		for (StatutBundleTopicObject bundleStatut : supervision.values()) {
			writer.append("<table align='left' style='border:1px solid grey; margin:10px;'>");
			writer.append("<tr colspan='2'>");
			writer.append("<td><b> [").append(bundleStatut.getBundle().getBundleId()).append("] ").append(bundleStatut.getBundle().getSymbolicName()).append(" ").append(bundleStatut.getBundle().getVersion()).append("</b></td>")
				.append("<td>[<span style='color:").append(OSGIStatusUtils.getBundleStatusStyleColor(bundleStatut.getBundle().getState())).append("'>").append(OSGIStatusUtils.getBundleStatusLibelle(bundleStatut.getBundle().getState())).append("</span>]</td>");
			writer.append("</tr>");
			writer.append("<tr><td>Heure de mise à jour</td><td>").append(DATE_MAJ_FORMAT.format(bundleStatut.getMiseAJour().getTime())).append("</td></tr>");
			writer.append("<tr><td><i>Statut des composants du bundle</i></td><td>[<span style='color:").append(OSGIStatusUtils.getBundleStatusStyleColor(bundleStatut.getStatutComponents())).append("'>" ).append(bundleStatut.getStatutComponents()).append("</span>] </td></tr>"); 
			writer.append("<tr><td></td></tr>");
			for (StatutPropertyBundleObject bundleValue : bundleStatut.getProperties()) {
				writer.append("<tr><td>- ").append(bundleValue.getLibelle()).append("</td>")
						.append("<td><span style='color:").append(OSGIStatusUtils.getBundleStatusStyleColor(bundleValue.getStatut())).append("'>" ).append(bundleValue.getValue()).append("</span></td></tr>");	
			}
			writer.append("</table>");
		}
	}

	/**
	 * Footer
	 * @param writer
	 */
	private void footer(final StringBuilder writer){
		writer.append("<br/>");

		writer.append("</body></html>");
	}
}