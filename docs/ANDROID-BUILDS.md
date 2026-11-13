# Builds Android Biznako (API 23 et API 29)

Le projet utilise les mêmes sources Capacitor pour les profils minimum Android 6 (API 23) et Android 10 (API 29). Les deux profils existent pour les builds debug et release. Le projet fixe minSdkVersion à 23 par défaut; le flavor api29 remplace ce minimum par 29. compileSdk 35 et targetSdk 35 ne changent pas le minimum d’installation.

## Versions utilisées

| Élément | Valeur du projet |
|---|---|
| Android Gradle Plugin | 8.7.2 |
| Gradle Wrapper | 8.11.1 |
| JDK | 21 |
| Android compileSdk / targetSdk | 35 / 35 |
| Android minSdk | API 23 : 23; API 29 : 29 |

AGP 8.7 nécessite Gradle 8.9 au minimum et JDK 17 au minimum; le wrapper 8.11.1 et JDK 21 sont utilisés ici.

## Préparer les sources

Depuis la racine du dépôt dans PowerShell :

~~~powershell
npm ci
npm test
npm run sync
~~~

npm run sync reconstruit dist/, puis synchronise les ressources Capacitor dans android/.

## APK debug dans Android Studio

1. Ouvrez le dossier android/ dans Android Studio.
2. Laissez Gradle Sync terminer avec le JDK 21.
3. Dans Build Variants, choisissez api23Debug ou api29Debug, puis utilisez Build > Build Bundle(s) / APK(s) > Build APK(s).
4. Vous pouvez aussi ouvrir un terminal à la racine et exécuter :

~~~powershell
cd android
.\gradlew.bat assembleApi23Debug assembleApi29Debug
~~~

Chemins des APK debug :

- android/app/build/outputs/apk/api23/debug/app-api23-debug.apk
- android/app/build/outputs/apk/api29/debug/app-api29-debug.apk

Ces APK debug sont destinées aux essais et portent une signature debug Android.

## Release signée localement

Ne mettez jamais le keystore ni ses mots de passe dans le dépôt. Conservez une seule clé de signature stable dans un emplacement sécurisé et sauvegardé. Dans PowerShell, définissez ces variables seulement dans la session de build :

~~~powershell
$env:BIZNAKO_KEYSTORE_FILE = "C:\chemin\securise\biznako-release.jks"
$env:ANDROID_KEY_ALIAS = "alias-de-la-cle"
$env:ANDROID_KEYSTORE_PASSWORD = "mot-de-passe-du-keystore"
$env:ANDROID_KEY_PASSWORD = "mot-de-passe-de-la-cle"
cd android
.\gradlew.bat assembleApi23Release assembleApi29Release -PreleaseVersionCode=2
~~~

Remplacez 2 par un entier positif supérieur au dernier versionCode publié. Le versionName reste 1.0.0; seul le versionCode des variantes release change. Effacez les variables de session après usage :

~~~powershell
Remove-Item Env:BIZNAKO_KEYSTORE_FILE, Env:ANDROID_KEY_ALIAS, Env:ANDROID_KEYSTORE_PASSWORD, Env:ANDROID_KEY_PASSWORD
~~~

Chemins attendus :

- android/app/build/outputs/apk/api23/release/app-api23-release.apk
- android/app/build/outputs/apk/api29/release/app-api29-release.apk

L’APK release signée est distincte d’une APK debug. Un AAB est un autre format de livraison, optimisé pour Google Play; ce workflow produit des APK et ne produit pas d’AAB.

## GitHub Actions et secrets

Le workflow .github/workflows/android.yml s’exécute à chaque push sur main et peut aussi être lancé depuis Actions > Biznako Android API 23 et 29 > Run workflow. Il installe les dépendances, teste le frontend, reconstruit et synchronise le web, compile les deux APK debug, vérifie leur présence et les publie dans l’artefact téléchargeable.

Pour obtenir aussi les deux APK release signées, configurez ces secrets au niveau de l’organisation ou du dépôt GitHub :

- ANDROID_KEYSTORE_BASE64 : keystore encodé en Base64, sur une seule ligne.
- ANDROID_KEY_ALIAS : alias de la clé.
- ANDROID_KEYSTORE_PASSWORD : mot de passe du keystore.
- ANDROID_KEY_PASSWORD : mot de passe de la clé.

Le workflow ne lit jamais leur valeur dans les logs. Si un secret manque, les APK debug sont quand même produites et le job indique que les releases ont été ignorées. Lorsque tous sont présents, le keystore est créé temporairement dans le runner, supprimé en fin de job, et les deux APK signées sont téléversées dans un artefact séparé. Le workflow n’embarque aucune clé ni mot de passe dans les sources.

Les artefacts sont dans la page du run Actions. Vérifiez que les deux debug sont toujours présents et que le run a indiqué la préparation de la signature avant de chercher l’artefact release. Une APK signée n’est pas validée pour production tant qu’elle n’a pas été installée et testée sur appareils/API 23 et API 29.

## Problèmes fréquents

- JDK incompatible : définissez Android Studio > Settings > Build Tools > Gradle > Gradle JDK sur JDK 21.
- SDK introuvable : installez Android SDK Platform 35 et Build Tools 35.0.0 dans SDK Manager; gardez local.properties local et non versionné.
- Gradle ne télécharge pas : vérifiez proxy/pare-feu; gardez le Gradle Wrapper et évitez de changer ses versions sans vérifier la compatibilité AGP.
- APK non trouvée : lisez la première erreur Gradle; le workflow échoue si une APK attendue manque.
- Signature différente à l’installation : la mise à jour doit utiliser la même clé. Sauvegardez les données JSON avant de désinstaller une version existante.
