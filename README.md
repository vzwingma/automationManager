# Automation Manager v1.4.0
Gestionnaire de tâches automatisées

[![Dependency Status](https://www.versioneye.com/user/projects/566f2ef71079970030000001/badge.svg?style=flat)](https://www.versioneye.com/user/projects/566f2ef71079970030000001)
<a href='https://travis-ci.org/vzwingma/automationManager'><img src='https://api.travis-ci.org/vzwingma/automationManager.svg?branch=master' alt='Build Status' /></a>
<a href='https://github.com/vzwingma/automationManager/issues'><img src='http://githubbadges.herokuapp.com/vzwingma/automationManager/issues?style=square' alt='Issues number' /></a>


## Modules OSGi

#### automationCommons v1.2.1
  *  Librairie commune
  * [![Dependency Status](https://www.versioneye.com/user/projects/566f2f0c107997003e000001/badge.svg?style=flat)](https://www.versioneye.com/user/projects/566f2f0c107997003e000001)

#### [supervisionBundle](https://github.com/vzwingma/automationManager/wiki/%5BBUNDLE%5D-Supervision) v1.3.0
  *  Libraire de supervision des bundles de l'AutomationManager
  *  [![Dependency Status](https://www.versioneye.com/user/projects/5675bcf2107997002d0008e4/badge.svg?style=flat)](https://www.versioneye.com/user/projects/5675bcf2107997002d0008e4)

#### [messagingBundle](https://github.com/vzwingma/automationManager/wiki/%5BBUNDLE%5D-Messaging) v1.1.1 
  *  Librairie d'envoi de mail et de SMS
  * [![Dependency Status](https://www.versioneye.com/user/projects/566f2f1b1079970030000014/badge.svg?style=flat)](https://www.versioneye.com/user/projects/566f2f1b1079970030000014)
    

#### [boxcryptorInventoryBundle](https://github.com/vzwingma/automationManager/wiki/%5BBUNDLE%5D-Boxcryptor-Inventory-Generator) v1.0.2
  *  Générateur d'inventaire pour BoxCryptor
  * [![Dependency Status](https://www.versioneye.com/user/projects/5675bcf1107997003e000859/badge.svg?style=flat)](https://www.versioneye.com/user/projects/5675bcf1107997003e000859)

#### [saveToBundle](https://github.com/vzwingma/automationManager/wiki/%5BBUNDLE%5D-SaveTo) 1.1.1
  *  Libraire de copie de fichier ou de répertoire
  *  [![Dependency Status](https://www.versioneye.com/user/projects/566f2f24107997003e000004/badge.svg?style=flat)](https://www.versioneye.com/user/projects/566f2f24107997003e000004)

## Installation des features

**Important : Dans le cas d'un upgrade de version, il est obligatoire de désinstaller les anciennes features, avant d'ajouter les nouvelles**

### Désinstallation des features

     feature:uninstall automation-supervision automation-messaging automation-boxcryptor-inventory automation-save-to
     feature:repo-remove automationManagerFeature

### Ajout du repository de features

#### à partir de GitHub

     feature:repo-add https://github.com/vzwingma/automationManager/releases/download/v1.4.0/feature.xml
     feature:install automation-manager
     
#### à partir de Maven

     feature:repo-add mvn:com.terrier.utilities.automation.features/automationManagerFeature/1.4.0/xml/features
     feature:install automation-supervision
     feature:install automation-messaging
     feature:install automation-boxcryptor-inventory
     feature:install automation-save-to