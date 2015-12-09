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

import java.util.Dictionary;

import com.terrier.utilities.automation.bundles.communs.AbstractAutomationActivator;
import com.terrier.utilities.automation.bundles.communs.enums.ConfigKeyEnums;
import com.terrier.utilities.automation.bundles.communs.exceptions.KeyNotFoundException;

/**
 * Activator du bundle
 * @author vzwingma
 *
 */
public class Activator extends AbstractAutomationActivator {


	private static Dictionary<String, String> dictionary;
	
	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.communs.AbstractAutomationActivator#getConfigurationPID()
	 */
	@Override
	public String getConfigurationPID() {
		return "com.terrier.utilities.automation.bundles.boxcryptor.save";
	}

	@Override
	public void updateDictionnary(Dictionary<String, String> dictionary) {
		Activator.dictionary = dictionary;
	}
	 
	
	/**
	 * @param key clé à charger du fichier
	 * @return valeur de la clé dans la configuration
	 */
	public static String getConfig(ConfigKeyEnums key) throws KeyNotFoundException{
		if(Activator.dictionary != null && key != null){
			return dictionary.get(key.getCodeKey());
		}
		else{
			throw new KeyNotFoundException(key != null ? key.getCodeKey() : null);
		}
			
	}
   
}