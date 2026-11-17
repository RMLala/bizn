# Biznako 1.2.0 — Android

Projet Android Capacitor à ouvrir dans Android Studio. Le code HTML source principal est src/index.html; le script de build le copie dans dist/ sans dépendance distante supplémentaire.

## Build local sous Windows

Prérequis : Node.js 22.22.2 ou supérieur, Android Studio avec JDK 21 et Android SDK Platform 35 / Build Tools 35.0.0.

Dans PowerShell, à la racine de ce dossier :

    npm.cmd ci
    npm.cmd run sync
    cd android
    .\gradlew.bat assembleApi23Debug assembleApi29Debug

APK Debug attendues :

- android\app\build\outputs\apk\api23\debug\app-api23-debug.apk
- android\app\build\outputs\apk\api29\debug\app-api29-debug.apk

API 23 = Android 6 et plus ; API 29 = Android 10 et plus. La version affichée est 1.2.0.

## Release signé

Le workflow GitHub Actions compile les deux Debug. Il compile aussi les deux Release si les secrets de l’organisation ou du dépôt sont disponibles : KEYSTORE_BASE64, KEY_ALIAS, KEYSTORE_PASSWORD et KEY_PASSWORD. Ne place jamais android-release.jks dans le dépôt.

Dans GitHub, lance l’action Biznako Android via Actions → Run workflow, puis récupère les APK dans Artifacts.

## Synchronisation et sauvegardes

AppForge Cloud synchronise les enregistrements après connexion et garde le fonctionnement local hors ligne. Les licences AppForge et l’accès Cloud automatique après activation ne sont pas encore intégrés dans Biznako. Google Drive est une sauvegarde facultative séparée. Les sauvegardes JSON peuvent être chiffrées facultativement avec un mot de passe (AES-GCM) ; les anciens exports JSON restent restaurables. Dans l’application Android, le partage ouvre le sélecteur système avec le fichier privé transmis par `FileProvider` (Google Drive peut être choisi parmi les cibles installées). Dans le navigateur, Web Share Files est utilisé lorsqu’il est pris en charge. Si le partage natif échoue, l’export Android via le sélecteur de document reste disponible. Aucun OAuth Google Drive n’est utilisé et ce mécanisme ne réalise pas une synchronisation Drive automatique. La restauration utilise le sélecteur de fichiers et demande confirmation avant remplacement des données locales.

Les builds actuels sont publiés dans les artefacts du workflow GitHub Actions de la branche concernée.


## APK API 23/29

Les quatre variantes, leurs chemins, les secrets de signature et le fonctionnement GitHub Actions sont documentés dans [docs/ANDROID-BUILDS.md](docs/ANDROID-BUILDS.md). Les tests et builds Debug se déclenchent aussi pour une pull request vers main; les Release signées s’ajoutent si les quatre secrets sont accessibles.

## Verrouillage Android

L’application Android demande la biométrie en priorité quand le système la propose, avec un PIN local de secours de 4 à 12 chiffres. Le vérificateur PIN est un HMAC protégé par une clé Android Keystore et les échecs répétés imposent un délai. Le verrouillage Android est déclenché à l’ouverture et au retour dans l’application. Le HTML ouvert dans un navigateur ne prétend pas fournir cette protection.

Ce mécanisme protège l’accès à l’interface sur un appareil Android standard ; il ne chiffre pas à lui seul les données IndexedDB. Les données métier et sauvegardes restent à protéger par les mécanismes de stockage/sauvegarde prévus.

## Personnaliser les rubriques métier

Dans Réglages → Type d’activité, choisissez commerce de biens, services ou mixte. Les sections adaptées au profil sont affichées ; changer le profil ou masquer une rubrique ne supprime aucun enregistrement. Réactivez une rubrique pour retrouver ses données. Les tables disponibles comprennent articles/produits, stock et lots, ventes, clients/contacts, fournisseurs, services/commissions, encaissements, dépenses, analyses et historique.

Les filtres des listes combinent activité, dates, statut, champ recherché (tous, nom, note ou référence) et tri par date, nom ou montant dans les deux sens. Enregistrez des filtres pour les réutiliser. La page « À propos & support » indique le mode de stockage, l’état réel de connexion Cloud, la version, les limites de confidentialité et le contact d’assistance.

Les profils de démonstration (biens, services, mixte) contiennent des exemples fictifs et remplacent la base courante après confirmation. Exportez d’abord une sauvegarde si vous voulez garder vos données.
