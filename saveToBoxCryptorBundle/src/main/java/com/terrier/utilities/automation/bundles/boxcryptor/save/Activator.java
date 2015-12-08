/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.terrier.utilities.automation.bundles.boxcryptor.save;

import java.util.Hashtable;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedService;

import com.terrier.utilities.automation.bundles.boxcryptor.save.business.BusinessService;
import com.terrier.utilities.automation.bundles.boxcryptor.save.config.ConfigUpdater;

/**
 * Activator du bundle
 * @author vzwingma
 *
 */
public class Activator implements BundleActivator {

	
	 private static final Logger LOGGER = Logger.getLogger( Activator.class );
	 
	 private static final String CONFIG_PID = "com.terrier.utilities.automation.bundles.boxcryptor.save";
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		LOGGER.info("Demarrage du bundle");
		
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		properties.put(Constants.SERVICE_PID, CONFIG_PID);
		context.registerService(ManagedService.class.getName(), new ConfigUpdater() , properties);
		LOGGER.info("Chargement du fichier de configuration /etc/" + CONFIG_PID + ".cfg");
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		LOGGER.info("Arrêt du bundle");
	}

   
}