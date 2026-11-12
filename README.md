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

Le workflow GitHub Actions compile les deux Debug. Il compile aussi les deux Release si les secrets de l’organisation ou du dépôt sont disponibles : ANDROID_KEYSTORE_BASE64, ANDROID_KEY_ALIAS, ANDROID_KEYSTORE_PASSWORD et ANDROID_KEY_PASSWORD. Ne place jamais android-release.jks dans le dépôt.

Dans GitHub, lance l’action Biznako Android via Actions → Run workflow, puis récupère les APK dans Artifacts.

## Synchronisation et sauvegardes

AppForge Cloud synchronise les enregistrements après connexion et garde le fonctionnement local hors ligne. Google Drive est une sauvegarde facultative séparée. Google OAuth doit être configuré et autorisé pour l’origine de l’application. Le fichier JSON sauvegardé n’est pas chiffré. L’ouverture directe du HTML en file:// ne permet pas Google OAuth ; l’APK n’a pas encore reçu de module Android natif Google OAuth dédié. Les sauvegardes JSON locales restent disponibles.

Le projet et le workflow n’ont pas été compilés dans cette session ; aucun APK n’est inclus dans ce dossier.
