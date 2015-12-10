package com.terrier.utilities.automation.bundles.messaging;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService;
import com.terrier.utilities.automation.bundles.communs.enums.messaging.EventsTopicNameEnum;

/**
 * Classe de service de messaging
 * @author vzwingma
 *
 */
@Singleton
public class MessagingBusinessService extends AbstractAutomationService {



	private static final Logger LOGGER = Logger.getLogger( MessagingBusinessService.class );
	
	
	@Inject private MessageEventHandler eventMessages;
	
	/**
	 * Initialisation
	 */
	@PostConstruct
	public void initService(){
		registerToConfig("com.terrier.utilities.automation.bundles.messaging");
		
		LOGGER.info("Enregistrement de l'eventHandler " + eventMessages + " sur le topic : " + EventsTopicNameEnum.NOTIFIFY_MESSAGE.getTopicName());
		Dictionary<String, String[]> props = new Hashtable<String, String[]>();
        props.put(EventConstants.EVENT_TOPIC, new String[]{EventsTopicNameEnum.NOTIFIFY_MESSAGE.getTopicName()});
		FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(EventHandler.class.getName(), eventMessages , props);
	}
	
	
	
	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService#notifyUpdateDictionnary()
	 */
	@Override
	public void notifyUpdateDictionnary() {
		// Pas de modification
		
	}

}
