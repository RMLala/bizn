# Builds Android Biznako 1.2.0 (API 23 et API 29)

Ce projet utilise un seul code source Capacitor et deux flavors Android :

| Variante | Android minimum | APK |
|---|---:|---|
| api23 | Android 6 / API 23 | Debug et Release |
| api29 | Android 10 / API 29 | Debug et Release |

Le projet utilise Android Gradle Plugin 8.7.2, Gradle Wrapper 8.11.1, JDK 21, compileSdk 35, targetSdk 35 et Build Tools 35.0.0. Le minSdk est défini séparément par flavor; ne le confondez pas avec compileSdk.

## Build local dans PowerShell

Depuis la racine du projet :

~~~powershell
npm.cmd ci
npm.cmd test
npm.cmd run sync
cd android
.\gradlew.bat assembleApi23Debug assembleApi29Debug
~~~

APK debug :

- android/app/build/outputs/apk/api23/debug/app-api23-debug.apk
- android/app/build/outputs/apk/api29/debug/app-api29-debug.apk

Ouvrez android/ dans Android Studio, choisissez JDK 21 comme Gradle JDK, puis choisissez api23Debug ou api29Debug dans Build Variants. Le Gradle Wrapper du projet doit rester la référence.

## Release signées

Ne commitez jamais le keystore. Pour une release locale, fournissez la signature via les paramètres Android Gradle ou l’assistant Generate Signed APK d’Android Studio. Le workflow automatise la signature avec les secrets GitHub ci-dessous. Il conserve le versionName 1.2.0 et passe le numéro d’exécution comme versionCode des seules variantes Release.

Chemins release attendus :

- android/app/build/outputs/apk/api23/release/app-api23-release.apk
- android/app/build/outputs/apk/api29/release/app-api29-release.apk

Les APK Debug sont signées avec la clé de développement. Les APK Release signées nécessitent votre clé privée stable. Un AAB est un format distinct destiné à Google Play; il n’est pas produit par ce workflow.

## GitHub Actions

Le workflow .github/workflows/android.yml s’exécute à chaque push sur main et prend en charge workflow_dispatch. Il exécute npm ci, npm test, npm run sync, compile les deux APK debug, vérifie les chemins attendus et téléverse un artefact. Les deux APK release sont également construites et téléversées si les quatre secrets sont présents.

Configurer les secrets au niveau de l’organisation ou du dépôt :

- ANDROID_KEYSTORE_BASE64 : contenu du keystore encodé en Base64 sur une ligne.
- ANDROID_KEY_ALIAS : alias de la clé.
- ANDROID_KEYSTORE_PASSWORD : mot de passe du keystore.
- ANDROID_KEY_PASSWORD : mot de passe de la clé.

Les secrets ne sont pas écrits dans le dépôt. Le keystore est placé temporairement dans RUNNER_TEMP et supprimé à la fin du job. Si un secret manque, le workflow produit tout de même les deux APK debug et affiche que les releases ont été ignorées. Les artifacts sont téléchargeables depuis la page du run Actions.

## Dépannage

- JDK incompatible : réglez Android Studio > Settings > Build Tools > Gradle > Gradle JDK sur 21.
- SDK manquant : installez Android SDK Platform 35 et Build Tools 35.0.0 dans SDK Manager.
- Gradle échoue au téléchargement : vérifiez le proxy et la connexion; gardez les versions du Wrapper et AGP cohérentes.
- APK attendue manquante : le workflow échoue explicitement avant l’envoi de l’artefact.
- Mise à jour refusée : une APK Release doit être signée avec la même clé que la version déjà installée. Exportez vos données JSON avant toute désinstallation.
