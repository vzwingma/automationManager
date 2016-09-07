/**
 * 
 */
package com.terrier.utilities.automation.bundles.supervision.business;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService;
import com.terrier.utilities.automation.bundles.communs.enums.messaging.EventsTopicNameEnum;
import com.terrier.utilities.automation.bundles.communs.enums.statut.StatutPropertyNameEnum;
import com.terrier.utilities.automation.bundles.communs.model.StatutBundleTopicObject;
import com.terrier.utilities.automation.bundles.communs.model.StatutPropertyBundleObject;
import com.terrier.utilities.automation.bundles.supervision.communs.OSGIStatusUtils;
import com.terrier.utilities.automation.bundles.supervision.listeners.AutomationBundlesListener;

/**
 * Supervision Service
 * @author vzwingma
 *
 */
@Singleton
public class SupervisionBusinessService extends AbstractAutomationService implements EventHandler {


	private static final Logger LOGGER = LoggerFactory.getLogger( SupervisionBusinessService.class );
	// Message Handler
	@Inject private AutomationBundlesListener automationBundlesListener;


	private static final Map<Long, StatutBundleTopicObject> MAP_SUPERVISION_BUNDLE = new HashMap<Long, StatutBundleTopicObject>();

	/**
	 * Initialisation
	 */
	@PostConstruct
	public void initService(){

		Dictionary<String, String[]> props = new Hashtable<String, String[]>();
		String[] listeTopics = new String[]{EventsTopicNameEnum.SUPERVISION_EVENTS.getTopicName()};
		props.put(EventConstants.EVENT_TOPIC, listeTopics);
		LOGGER.info("Enregistrement de l'eventHandler {} sur le topic : {}", this, EventsTopicNameEnum.SUPERVISION_EVENTS.getTopicName());
		FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(EventHandler.class.getName(), this , props);

		// Supervision des bundles et des services
		FrameworkUtil.getBundle(this.getClass()).getBundleContext().addBundleListener(automationBundlesListener);
		FrameworkUtil.getBundle(this.getClass()).getBundleContext().addServiceListener(automationBundlesListener);
	}


	@Override
	public void notifyUpdateDictionary() {
		// Rien car il n'y a pas de fichier de configuration associé		
	}


	/* (non-Javadoc)
	 * @see org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event.Event)
	 */
	@Override
	public void handleEvent(Event event) {
		LOGGER.debug("Topic [{}] Réception du message [{}]", event.getTopic(), event);

		
		StatutBundleTopicObject statutBundleObject = (StatutBundleTopicObject)event.getProperty(StatutPropertyNameEnum.STATUS.getName());
		long time = (long)event.getProperty(StatutPropertyNameEnum.TIME.getName());
		statutBundleObject.getMiseAJour().setTimeInMillis(time);
		MAP_SUPERVISION_BUNDLE.put(statutBundleObject.getBundle().getBundleId(), statutBundleObject);
		LOGGER.info(logStatut(statutBundleObject));
	}

	/**
	 * Affichage du statut
	 * @param statutBundle statut du bundle reçu
	 */
	protected String logStatut(StatutBundleTopicObject statutBundle){

		StringBuilder log = new StringBuilder("\n> Statut de ").append(statutBundle.getBundle().getSymbolicName()).append(" : ").append(OSGIStatusUtils.getBundleStatusLibelle(statutBundle.getBundle().getState())).append("\n");

		List<StatutPropertyBundleObject> statutMap = statutBundle.getProperties();
		for (StatutPropertyBundleObject statutEntry : statutMap) {
			log.append("     ").append(statutEntry);	
		}

		return log.toString();
	}

	/**
	 * @return le statut des bundles
	 */
	public static final Map<Long, StatutBundleTopicObject> getStatutBundles(){
		return MAP_SUPERVISION_BUNDLE;
	}


	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService#updateSupervisionEvents(java.util.List)
	 */
	@Override
	public void updateSupervisionEvents(List<StatutPropertyBundleObject> supervisionEvents) { }
}
