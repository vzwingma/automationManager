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
import com.terrier.utilities.automation.bundles.supervision.communs.PresentationStatutModuleEnum;
import com.terrier.utilities.automation.bundles.supervision.model.PresentationStatutObject;
import com.terrier.utilities.automation.bundles.supervision.model.PresentationStatutObjectTranslator;
import com.terrier.utilities.automation.bundles.supervision.model.PresentationStatutPropertyObject;


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
		writer.append("<html>");
		css(writer);
		writer.append("<body align='center'>");
		writer.append("<h1>Supervision de l'AutomationManager</h1>");
	}

	/**
	 * CSS
	 * @param writer
	 */
	private void css(final StringBuilder writer){
		writer.append("<head><style>").append("\n");
		writer.append("table.bundletab { border:1px solid grey; margin:10px; border-collapse:collapse; align:left; width:100%}").append("\n");
		writer.append("tr.bundletitle { font-size:22px; font-weight:bold; background-color:#81BEF7; }").append("\n");
		writer.append("tr.componentstitle { font-weight:bold; font-style:italic; font-size:18px }").append("\n");
		writer.append("td { width:50% }").append("\n");
		// Statut des process
		writer.append("span.status_OK { color:green; }").append("\n");
		writer.append("span.status_WARNING { color:orange; }").append("\n");
		writer.append("span.status_ERROR { color:red; }").append("\n");
		// Statut des bundles
		writer.append("span.status_").append(PresentationStatutModuleEnum.INSTALLE).append(" { color:grey; }").append("\n");
		writer.append("span.status_").append(PresentationStatutModuleEnum.DESINSTALLE).append(" { color:grey; }").append("\n");
		writer.append("span.status_").append(PresentationStatutModuleEnum.INCONNU).append(" { color:grey; }").append("\n");
		writer.append("span.status_").append(PresentationStatutModuleEnum.STOPPE).append(" { color:red; }").append("\n");
		writer.append("span.status_").append(PresentationStatutModuleEnum.DEMARRE).append(" { color:green; }").append("\n");
		writer.append("span.status_").append(PresentationStatutModuleEnum.DEMARRAGE).append(" { color:orange; }").append("\n");
		writer.append("span.status_").append(PresentationStatutModuleEnum.ARRET).append(" { color:orange; }").append("\n");
		writer.append("</style></head>");
	}


	/**
	 * Page de statut
	 * @param writer
	 */
	private void statutPage(final StringBuilder writer){
		// Groupe bundles
		writer.append("<div style='width:650px'>");
		Map<Long, StatutBundleTopicObject> supervision = BundleSupervisionBusinessService.getStatutBundles();
		for (StatutBundleTopicObject bundleStatut : supervision.values()) {
			statutComponent(writer, PresentationStatutObjectTranslator.translateFrom(bundleStatut));
		}
		writer.append("</div>");
	}

	/**
	 * @param writer writer
	 * @param bundlePresentationStatut  statut component
	 */
	public void statutComponent(final StringBuilder writer, PresentationStatutObject bundlePresentationStatut){
		writer.append("<table  class='bundletab'>");
		writer.append("<tr colspan='2' class='bundletitle'>");
		writer.append("<td>").append(bundlePresentationStatut.getNomModule()).append("</td>")
		.append("<td>[<span class=status_'").append(bundlePresentationStatut.getEtatModule()).append("'>").append(bundlePresentationStatut.getEtatModule()).append("</span>]</td>");
		writer.append("</tr>");
		writer.append("<tr><td>Heure de mise à jour</td><td>").append(bundlePresentationStatut.getDateMiseAJour() != null ? DATE_MAJ_FORMAT.format(bundlePresentationStatut.getDateMiseAJour().getTime()) : "???").append("</td></tr>");
		writer.append("<tr class='componentstitle'><td>Statut des composants</i></td><td>[<span class='status_").append(bundlePresentationStatut.getStatutComponents()).append("'>" ).append(bundlePresentationStatut.getStatutComponents()).append("</span>] </td></tr>"); 
		writer.append("<tr><td></td></tr>");
		for (PresentationStatutPropertyObject bundleValue : bundlePresentationStatut.getListStatutPropertyObject()) {
			writer.append("<tr><td>- ").append(bundleValue.getNom()).append("</td>")
			.append("<td><span class='status_").append(bundleValue.getEtat()).append("'>" ).append(bundleValue.getValeur()).append("</span></td></tr>");	
		}
		writer.append("</table>");
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