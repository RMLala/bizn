# Biznako 1.0.0 — guide de démarrage

Application de gestion locale : yaourts, commissions immobilières et activités personnalisables. Interface français/malagasy, Ariary, thèmes clair/sombre. Aucun abonnement ni serveur requis pour les fonctions livrées.

## 1. Essayer sans installer

Ouvrez **dist/Biznako.html** dans Chrome ou Edge sur ordinateur. Le fichier contient tout le CSS et JavaScript : aucun CDN, police ou icône distante. Au premier lancement, choisissez vos données ou la démonstration. Les exemples sont fictifs et identifiés par une bannière DÉMO.

Le stockage utilise IndexedDB dans ce navigateur et sur cet appareil. Si IndexedDB est refusé lors de l’ouverture directe du HTML, l’application tente automatiquement un stockage local de secours. Le mode privé, un navigateur intégré à une messagerie ou les restrictions sur les fichiers locaux peuvent empêcher toute persistance. L’application affiche alors une erreur sans écraser la base. Sur Android, privilégiez l’APK après compilation ; certains gestionnaires de fichiers ouvrent un aperçu HTML qui ne permet pas IndexedDB. Gardez le même fichier et navigateur, et exportez avant de le déplacer ou le renommer. Les données du HTML et de l’APK sont séparées : utilisez JSON pour les transférer.

## 2. Parcours simple

1. **Contacts** : ajoutez vos clients et leurs numéros.
2. **Produits** : créez vos yaourts, prix par pot et seuils d’alerte.
3. **Stock & lots** : saisissez chaque production, quantité, coût unitaire et péremption. Le champ pertes cumule les pots jetés de ce lot. Une perte ne peut pas retirer des pots déjà vendus.
4. **Ventes** : choisissez un client et les lots, ajoutez plusieurs lignes si nécessaire. Le statut Brouillon n’a aucun effet. Valider consomme le stock et reconnaît la vente.
5. **À encaisser** : cliquez sur Encaisser et saisissez le montant réellement reçu. Les paiements partiels sont possibles. Une vente validée n’est pas automatiquement payée.
6. **Immobilier & services** : renseignez base, taux et votre part. Exemple : 1 200 000 Ar × 50 % × 50 % = 300 000 Ar pour vous. Validez lorsque la commission est acquise, puis enregistrez le paiement.
7. **Dépenses** : enregistrez vos décaissements. Un statut validé signifie que la dépense est payée ; gardez en brouillon si elle est seulement prévue.
8. **Réglages** : sauvegardez en JSON régulièrement, et copiez ce fichier hors du téléphone.

Pour une prestation à prix fixe, utilisez son prix comme base et 100 % pour chacun des deux pourcentages. Pour ajouter une activité : Réglages → Mes activités → +. Les libellés de vos activités, contacts et statuts personnalisés ne sont pas traduits automatiquement.

Les menus ⋯ donnent les actions disponibles. Une suppression de produit, lot, contact, activité ou document encore référencé est refusée. Pour annuler un document déjà payé, corrigez d’abord ses encaissements, puis son statut. La flèche ↶ annule la dernière écriture de la session avec confirmation. La copie locale précédente reste récupérable dans Réglages. Le journal conserve les 5 000 dernières actions ; il n’est pas certifié.

## 3. Comprendre les chiffres

- Ventes validées : somme quantité × prix des lignes validées.
- Commissions acquises : base × taux / 100 × votre part / 100, arrondie à l’Ariary.
- Marge brute produits : ventes validées − coût des unités vendues, suivant le coût du lot choisi.
- Flux net : encaissements datés dans la période − dépenses payées datées dans la période. **Ce n’est pas un solde bancaire**, aucun solde d’ouverture n’est supposé.
- Reste à encaisser : montant des documents validés de la période moins tous leurs paiements enregistrés à ce jour. Ce n’est pas une balance historique arrêtée à une date antérieure.
- Stock : quantité du lot − pertes − quantités des ventes validées. Les lots périmés restent dans la liste, mais ne sont plus vendables après leur péremption. Les alertes de disponibilité excluent les lots futurs/périmés.

Le coût du lot n’est pas automatiquement une sortie de trésorerie : saisissez le paiement de production dans Dépenses. Ne déduisez pas une seconde fois ces mêmes coûts pour interpréter la marge. Les pertes diminuent le stock ; la marge brute affichée n’intègre pas de charge supplémentaire de dépréciation. Modifier le coût d’un lot recalcule la marge de ses ventes passées : exportez un état avant une correction importante.

Les filtres du tableau de bord portent sur les dates des documents ; les flux portent sur les dates des paiements/dépenses. Les filtres activité/date restent en place lors du passage entre vues. Les tableaux affichent 15 lignes par page ; leurs exports reprennent toutes les lignes filtrées. Le total des listes inclut tous les statuts visibles, explicitement indiqué. Les KPI ne retiennent que les statuts à effet validé.

## 4. Sauvegarde, fichiers et PDF

- JSON : sauvegarde complète, import/restauration après validation de version, structure, dates, montants, références, stocks et paiements. Limite d’import : 10 Mo. Une restauration remplace la base après confirmation ; elle ne fusionne pas les appareils.
- CSV : export des listes filtrées, flux et journal, séparateur point-virgule, UTF-8 avec BOM pour Excel. Les cellules pouvant être interprétées comme formules sont neutralisées. **L’import CSV n’est pas implémenté** : l’import complet passe par JSON.
- PDF : ouvrez un document ou une liste puis Imprimer / PDF. Dans Chrome/Edge, choisissez « Enregistrer au format PDF ». Dans l’APK, la boîte d’impression native Android propose « Enregistrer au format PDF » si le service système est disponible. Ce n’est pas une bibliothèque PDF embarquée.
- Les documents sont des relevés de gestion internes, pas des factures fiscales certifiées.
- Le JSON n’est pas chiffré. Les données restent locales tant que AppForge Cloud n’est pas configuré. Après activation facultative, la synchronisation Cloudflare fonctionne hors ligne-first. Android utilise le sélecteur de documents système pour exporter, sans autorisation générale de stockage.
- Réinitialisation/démo : confirmation explicite, puis EFFACER pour vider la base. Le chargement d’exemples remplace les données ; exportez auparavant.

## 5. Choix techniques vérifiés

| Élément | Version retenue / rôle |
|---|---|
| Frontend | HTML, CSS et JavaScript natif ; assemblage Node sans bundler |
| Stockage | IndexedDB, schéma 1, écriture atomique avec copie précédente et contrôle de révision |
| Capacitor core / Android / CLI | 7.6.9, versions exactes et lock npm |
| Node.js | 22 recommandé ; Capacitor 7 exige 20 minimum |
| Java | JDK 21 pour Capacitor 7 (AGP seul accepte 17, mais cela ne suffit pas ici) |
| AGP | 8.7.2 |
| Gradle Wrapper | 8.11.1, JAR et scripts Unix/Windows inclus |
| minSdk | flavor api23 : 23; flavor api29 : 29 |
| compileSdk / targetSdk | 35 / 35 |
| Build Tools | 34.0.0, valeur attendue par AGP 8.7 |
| App ID | `mg.biznako.gestion`, identifiant proposé à réserver pour votre distribution |
| Version | versionName 1.0.0, versionCode 1 |

Capacitor 8 impose minSdk 24 : il ne répond pas au profil Android 6 demandé. La combinaison de la référence HabitPulse (JDK17/SDK34/Gradle8.2.1) n’a donc pas été mélangée avec Capacitor 7.

Sources officielles consultées le 03/11/2026 :
- https://capacitorjs.com/docs/updating/7-0 — Node20, JDK21, API23/35, AGP8.7.2, Gradle8.11.1.
- https://capacitorjs.com/docs/updating/8-0 — minimum API24 de Capacitor8.
- https://developer.android.com/build/releases/agp-8-7-0-release-notes — API35, Gradle8.9 minimum, Build Tools34.
- https://developer.android.com/build/releases/about-agp — compatibilité Android Studio / AGP. Les versions stables récentes listées acceptent AGP8.7. Android Studio doit être au minimum Ladybug 2024.2.1 pour ce projet. Installez le canal stable depuis le site officiel et choisissez explicitement JDK21, même si l’IDE embarque un autre JDK.

La livraison vise installation directe/debug, pas une publication Google Play certifiée. Les exigences Play changent : vérifiez-les avant publication, sans relever aveuglément le targetSdk ou minSdk. Android 6 doit disposer d’un Android System WebView suffisamment à jour pour les fonctions JavaScript utilisées ; le minSdk seul ne garantit pas un WebView récent.

## 6. Windows 11 : installer et ouvrir Android Studio

Prérequis : Node.js 22, Android Studio stable, JDK21 sélectionné et Android SDK35 installé. Une connexion Internet est nécessaire à la première installation npm/Gradle/SDK, pas à l’utilisation de l’application.

Décompressez le ZIP, par exemple dans C:\\Projets\\Biznako. Ouvrez PowerShell dans ce dossier, celui contenant package.json.

~~~powershell
cd C:\\Projets\\Biznako
npm.cmd ci
npm.cmd run build
npm.cmd test
npm.cmd run sync
npm.cmd run open
~~~

npm.cmd évite le blocage PowerShell de npm.ps1. npm run sync construit le HTML puis exécute cap sync android. webDir vaut dist. Le dossier android/ est déjà créé et synchronisé : ne lancez pas npx cap add android.

Dans Android Studio, ouvrez le dossier android/. Choisissez JDK21 dans Settings > Build Tools > Gradle. Dans SDK Manager, installez Android SDK Platform 35, Build Tools 34.0.0 et Platform-Tools. Gardez le Gradle Wrapper et les versions du projet.

Les Build Variants api23Debug et api29Debug produisent les APK Android 6 et Android 10. En terminal :

~~~powershell
cd C:\\Projets\\Biznako\\android
.\\gradlew.bat assembleApi23Debug assembleApi29Debug
~~~

Après une modification dans src/, revenez à la racine, exécutez npm.cmd run sync, puis recompilez. Modifier uniquement dist/ serait écrasé au prochain build.

## 7. GitHub Actions : les mêmes sources

Le dépôt contient déjà le workflow .github/workflows/android.yml. Il se déclenche à chaque push sur main et via Actions > Biznako Android API 23 et 29 > Run workflow. La PR de correction se compile après intégration ou peut être lancée manuellement sur sa branche.

Le workflow installe Node22, JDK21 et SDK35, exécute npm ci et npm test, reconstruit le web, synchronise Capacitor, puis compile les deux APK debug. Il vérifie les deux chemins avant de déposer l’artefact Biznako-1.0.0-debug-API23-API29.

Pour obtenir aussi les APK release signées, configurez les quatre secrets Android décrits dans docs/ANDROID-BUILDS.md. Si l’un manque, le workflow conserve les APK debug et signale que les releases ont été ignorées. Les artefacts sont téléchargeables depuis la page du run Actions.

## 8. Installer sur téléphone

Téléchargez l’APK correspondant au profil du téléphone depuis l’artefact Actions et copiez-la sur le téléphone. Autorisez temporairement l’installation depuis cette source si Android le demande. API23 fonctionne à partir d’Android 6; API29 à partir d’Android 10.

Chemins debug : android/app/build/outputs/apk/api23/debug/app-api23-debug.apk et android/app/build/outputs/apk/api29/debug/app-api29-debug.apk. Pour installer par USB, utilisez adb install -r avec le chemin du profil choisi.

Pour conserver les données lors des mises à jour, il faut garder le même App ID, la même signature et un versionCode croissant. Les signatures debug peuvent différer entre exécutions. Exportez votre JSON avant de désinstaller.

## 9. Debug, release signé et AAB

- Les APK debug API23 et API29 servent aux essais.
- Les APK release signées API23 et API29 sont destinées à la distribution directe durable et nécessitent le keystore privé configuré dans les secrets.
- Un AAB est le format destiné à une boutique. Ce workflow ne produit pas d’AAB.

Les commandes PowerShell, les chemins exacts, l’incrémentation du versionCode release et la configuration sécurisée des secrets sont décrits dans docs/ANDROID-BUILDS.md. Le versionName reste 1.0.0; seul le versionCode release augmente. Ne mettez jamais un keystore ou ses mots de passe dans le dépôt.

## 10. Permissions et fonctions Android

- INTERNET : permission standard du conteneur Capacitor. Le pilote peut appeler le Worker AppForge Cloud via HTTPS pour la synchronisation; aucune connexion Google OAuth n’est requise. Sans réseau ou si le service est indisponible, Biznako conserve les données localement.
- POST_NOTIFICATIONS : demandée seulement à l’activation d’un rappel, à partir d’Android13.
- Aucune permission caméra, micro, contacts, localisation ni accès global aux fichiers.
- Export : Storage Access Framework ; choix de destination explicite.
- Rappel : une notification locale prévue pour le lendemain vers 18 h. Alarme non exacte, susceptible d’être retardée par l’économie d’énergie. Un nouveau rappel remplace le précédent. Il ne survit pas au redémarrage du téléphone ; réactivez-le ensuite. Aucun réveil exact ni accès spécial demandé.
- Retour Android : ferme le dialogue, revient au tableau de bord, puis demande confirmation pour quitter.
- Sauvegarde automatique cloud Android désactivée dans le manifeste. La base locale et la copie précédente restent locales. Aucun verrou PIN/biométrique n’est présenté comme un chiffrement.

## 11. Dépannage

| Symptôme | Vérification / action |
|---|---|
| npm : lock file not found | package-lock.json doit être à la racine avec package.json. Ne téléversez pas seulement android/. |
| npm.ps1 bloqué | Utilisez npm.cmd dans PowerShell. |
| invalid source release: 21 | JAVA_HOME / Gradle JDK pointe vers Java17 : sélectionnez JDK21. |
| SDK location not found | Ouvrez android/ dans Android Studio, installez le SDK et laissez l’IDE créer local.properties. |
| platform android-35 absent | SDK Manager → Android35 ; ou sdkmanager "platforms;android-35". |
| Téléchargement Gradle/Maven échoue | Vérifiez connexion, proxy/pare-feu, certificats et réessayez sans changer les versions au hasard. |
| APK introuvable | Lisez la première erreur de compilation. Chemins attendus : android/app/build/outputs/apk/api23/debug/app-api23-debug.apk et android/app/build/outputs/apk/api29/debug/app-api29-debug.apk. |
| Application incompatible | Android doit être API23 minimum ; vérifiez aussi Android System WebView. |
| Installation en conflit | Signature différente : sauvegardez JSON avant de désinstaller. Utilisez ensuite une clé release stable. |
| Export Android semble bloqué | Le sélecteur de destination attend votre choix. Annuler renvoie un message explicite. |
| Rappel absent | Vérifiez l’autorisation notifications et l’économie d’énergie ; réactivez après redémarrage. |
| Écriture refusée | Stockage saturé/restreint ou autre fenêtre ouverte. Exportez, fermez les autres fenêtres puis rechargez. |
| Base corrompue | L’écran d’erreur permet l’export brut quand la base reste accessible ; ne videz pas le navigateur avant récupération. |

## 12. Limites et vérifications

Voir docs/TESTS.md pour le résultat réellement obtenu et la checklist. HTML construit, tests automatisés exécutés et parcours navigateur contrôlés. **APK non compilé ici : JDK21 et SDK35 absents dans l’environnement.** Le projet Android généré, le Wrapper JAR, le plugin natif et le workflow sont fournis, mais leur compilation et les essais physiques restent à exécuter. Aucun APK ou AAB précompilé n’est annoncé.

Pilote AppForge Cloud : le code Worker/D1 et le client Web/APK sont préparés mais le Worker n’est pas déployé. Le mode local fonctionne sans compte ni réseau. Une fois le Worker configuré, la synchronisation Cloudflare est automatique et sans Google OAuth obligatoire. L’APK nécessite toujours un accès HTTPS au Worker pour le cloud. Les essais sur deux comptes/appareils restent à effectuer avant de déclarer cette synchronisation vérifiée. Voir docs/APPFORGE-CLOUD-PILOT.md et docs/CLOUD-SECURITE.md. Google Drive reste une sauvegarde facultative distincte. L’assistant local fonctionne avec des règles de calcul ; ce n’est pas une IA générative. La comptabilité générale et la gestion de TVA ne sont pas incluses. Données raisonnablement limitées : validation à 20 000 lignes par table et import 10 Mo, sans benchmark de charge effectué. La validation complète à chaque écriture privilégie la cohérence ; pour un gros volume, faire évoluer le stockage vers des tables indexées et une API.


## 13. Profils Android 6/10 et quatre APK

Les profils API 23 et API 29 produisent chacun une APK debug; les secrets de signature permettent aussi les deux APK release. Voir [docs/ANDROID-BUILDS.md](docs/ANDROID-BUILDS.md) pour les commandes, les chemins, les secrets GitHub et le dépannage.
