# Automation Manager v${project.version}
Gestionnaire de tâches automatisées

<a href='https://travis-ci.org/vzwingma/automationManager'><img src='https://api.travis-ci.org/vzwingma/automationManager.svg?branch=master' alt='Build Status' /></a>
<a href='https://github.com/vzwingma/automationManager/issues'><img src='http://githubbadges.herokuapp.com/vzwingma/automationManager/issues?style=square' alt='Issues number' /></a>

[![Known Vulnerabilities](https://snyk.io/test/github/vzwingma/automationmanager/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/vzwingma/automationmanager?targetFile=pom.xml)
[![Dependency Status](https://img.shields.io/librariesio/github/vzwingma/automationManager.svg)](https://libraries.io/github/vzwingma/automationManager)

<a href="https://sonarcloud.io/dashboard?id=automationManager"><img alt="Sonar Build Status" src="https://sonarcloud.io/api/project_badges/measure?project=automationManager&metric=coverage" /></a>
<a href="https://sonarcloud.io/dashboard?id=automationManager"><img alt="Sonar Build Status" src="https://sonarcloud.io/api/project_badges/measure?project=automationManager&metric=sqale_rating" /></a>
<a href="https://sonarcloud.io/dashboard?id=automationManager"><img alt="Sonar Build Status" src="https://sonarcloud.io/api/project_badges/measure?project=automationManager&metric=reliability_rating" /></a>
<a href="https://sonarcloud.io/dashboard?id=automationManager"><img alt="Sonar Build Status" src="https://sonarcloud.io/api/project_badges/measure?project=automationManager&metric=security_rating" /></a>


## Modules OSGi

#### automationCommons v${automation.commons.version}
  *  Librairie commune
  *  [![Known Vulnerabilities](https://snyk.io/test/github/vzwingma/automationmanager/badge.svg?targetFile=automationCommons%2Fpom.xml)](https://snyk.io/test/github/vzwingma/automationmanager?targetFile=automationCommons%2Fpom.xml)

#### [supervisionBundle](https://github.com/vzwingma/automationManager/wiki/%5BBUNDLE%5D-Supervision) v${automation.supervision.version}
  *  Libraire de supervision des bundles de l'AutomationManager
  *  [![Known Vulnerabilities](https://snyk.io/test/github/vzwingma/automationmanager/badge.svg?targetFile=supervisionBundle%2Fpom.xml)](https://snyk.io/test/github/vzwingma/automationmanager?targetFile=supervisionBundle%2Fpom.xml)

#### [messagingBundle](https://github.com/vzwingma/automationManager/wiki/%5BBUNDLE%5D-Messaging) v${automation.messaging.version} 
  *  Librairie d'envoi de mail et de SMS
  *  [![Known Vulnerabilities](https://snyk.io/test/github/vzwingma/automationmanager/badge.svg?targetFile=messagingBundle%2Fpom.xml)](https://snyk.io/test/github/vzwingma/automationmanager?targetFile=messagingBundle%2Fpom.xml)

#### [emailsWorkerBundle](https://github.com/vzwingma/automationManager/wiki/%5BBUNDLE%5D-Boxcryptor-Inventory-Generator) v${automation.emails.worker.version}
  *  Traitement des emails reçus
  *  [![Known Vulnerabilities](https://snyk.io/test/github/vzwingma/automationmanager/badge.svg?targetFile=emailsWorkerBundle%2Fpom.xml)](https://snyk.io/test/github/vzwingma/automationmanager?targetFile=emailsWorkerBundle%2Fpom.xml)

#### [saveToBundle](https://github.com/vzwingma/automationManager/wiki/%5BBUNDLE%5D-SaveTo) ${automation.save.to.version}
  *  Libraire de copie de fichier ou de répertoire
  *  [![Known Vulnerabilities](https://snyk.io/test/github/vzwingma/automationmanager/badge.svg?targetFile=saveToBundle%2Fpom.xml)](https://snyk.io/test/github/vzwingma/automationmanager?targetFile=saveToBundle%2Fpom.xml)

## Installation des features

**Important : Dans le cas d'un upgrade de version, il est obligatoire de désinstaller les anciennes features, avant d'ajouter les nouvelles**

### Désinstallation des features

     feature:uninstall automation-supervision automation-messaging automation-save-to
     feature:repo-remove automationManagerFeature

### Ajout du repository de features

#### à partir de GitHub

     feature:repo-add https://github.com/vzwingma/automationManager/releases/download/v${project.version}/feature.xml
     feature:install automation-manager
     
#### à partir de Maven

     feature:repo-add mvn:com.terrier.utilities.automation.features/automationManagerFeature/${project.version}/xml/features
     feature:install automation-supervision
     feature:install automation-messaging
     feature:install automation-emails-worker
     feature:install automation-save-to