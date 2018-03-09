# Automation Manager v1.6.1
Gestionnaire de tâches automatisées

<a href='https://travis-ci.org/vzwingma/automationManager'><img src='https://api.travis-ci.org/vzwingma/automationManager.svg?branch=master' alt='Build Status' /></a>
<a href='https://github.com/vzwingma/automationManager/issues'><img src='http://githubbadges.herokuapp.com/vzwingma/automationManager/issues?style=square' alt='Issues number' /></a>

[![Known Vulnerabilities](https://snyk.io/test/github/vzwingma/automationmanager/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/vzwingma/automationmanager?targetFile=pom.xml)
[![Dependency Status](https://beta.gemnasium.com/badges/github.com/vzwingma/automationManager.svg)](https://beta.gemnasium.com/projects/github.com/vzwingma/automationManager)

<a href="https://scan.coverity.com/projects/vzwingma-automationmanager"><img alt="Coverity Scan Build Status" src="https://img.shields.io/coverity/scan/7397.svg"/></a>
<a href="https://sonarcloud.io/dashboard?id=automationManager"><img alt="Sonar Build Status" src="https://sonarcloud.io/api/badges/gate?key=automationManager"/></a>


## Modules OSGi

#### automationCommons v1.2.2
  *  Librairie commune
  *  [![Known Vulnerabilities](https://snyk.io/test/github/vzwingma/automationmanager/badge.svg?targetFile=automationCommons%2Fpom.xml)](https://snyk.io/test/github/vzwingma/automationmanager?targetFile=automationCommons%2Fpom.xml)

#### [supervisionBundle](https://github.com/vzwingma/automationManager/wiki/%5BBUNDLE%5D-Supervision) v1.3.1
  *  Libraire de supervision des bundles de l'AutomationManager
  *  [![Known Vulnerabilities](https://snyk.io/test/github/vzwingma/automationmanager/badge.svg?targetFile=supervisionBundle%2Fpom.xml)](https://snyk.io/test/github/vzwingma/automationmanager?targetFile=supervisionBundle%2Fpom.xml)

#### [messagingBundle](https://github.com/vzwingma/automationManager/wiki/%5BBUNDLE%5D-Messaging) v1.2.2 
  *  Librairie d'envoi de mail et de SMS
  *  [![Known Vulnerabilities](https://snyk.io/test/github/vzwingma/automationmanager/badge.svg?targetFile=messagingBundle%2Fpom.xml)](https://snyk.io/test/github/vzwingma/automationmanager?targetFile=messagingBundle%2Fpom.xml)

#### [boxcryptorInventoryBundle](https://github.com/vzwingma/automationManager/wiki/%5BBUNDLE%5D-Boxcryptor-Inventory-Generator) v2.0.1
  *  Générateur d'inventaire pour BoxCryptor
  *  [![Known Vulnerabilities](https://snyk.io/test/github/vzwingma/automationmanager/badge.svg?targetFile=boxcryptorInventoryGeneratorBundle%2Fpom.xml)](https://snyk.io/test/github/vzwingma/automationmanager?targetFile=boxcryptorInventoryGeneratorBundle%2Fpom.xml)

#### [saveToBundle](https://github.com/vzwingma/automationManager/wiki/%5BBUNDLE%5D-SaveTo) 1.1.4
  *  Libraire de copie de fichier ou de répertoire
  *  [![Known Vulnerabilities](https://snyk.io/test/github/vzwingma/automationmanager/badge.svg?targetFile=saveToBundle%2Fpom.xml)](https://snyk.io/test/github/vzwingma/automationmanager?targetFile=saveToBundle%2Fpom.xml)

## Installation des features

**Important : Dans le cas d'un upgrade de version, il est obligatoire de désinstaller les anciennes features, avant d'ajouter les nouvelles**

### Désinstallation des features

     feature:uninstall automation-supervision automation-messaging automation-boxcryptor-inventory automation-save-to
     feature:repo-remove automationManagerFeature

### Ajout du repository de features

#### à partir de GitHub

     feature:repo-add https://github.com/vzwingma/automationManager/releases/download/v1.6.1/feature.xml
     feature:install automation-manager
     
#### à partir de Maven

     feature:repo-add mvn:com.terrier.utilities.automation.features/automationManagerFeature/1.6.1/xml/features
     feature:install automation-supervision
     feature:install automation-messaging
     feature:install automation-boxcryptor-inventory
     feature:install automation-save-to