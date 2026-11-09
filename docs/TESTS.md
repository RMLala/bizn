# Tests et contrôle qualité

## Exécutions réalisées le 04/11/2026

- `npm run build` : réussi ; produit `dist/index.html` et `dist/Biznako.html`, sans ressource distante.
- `npm test` : 18 tests automatisés réussis. Ils couvrent KPI, commission, flux, pertes, ventes multi-lots, stock, dates, états brouillon/validé, encaissement partiel, dépassement, suppression référencée, règles personnalisées, dates de filtres, validation JSON, CSV protégé contre formules, parsing des deux scripts intégrés et contrôle de fichiers Android.
- Contrôle navigateur Chromium : ouverture, création et rechargement des données, affichage Dashboard, liste, filtres, formulaire multi-lignes, refus d’une quantité dépassant le lot, paiement partiel, mise à jour du restant, menu ⋯, français/malagasy, thème sombre et affichage à 390 px. La base de démonstration de QA est fictive.
- `npm run doctor` : Node 24 détecté ; Java17 détecté, JDK21 absent ; SDK Android35 absent. Le diagnostic retourne donc correctement une erreur pour la compilation locale.
- L’APK n’a pas été compilé dans cet environnement. Le workflow GitHub est fourni mais n’a pas été déclenché sur un compte réel.

## Checklist à suivre sur le poste Windows

### Données métier

- [ ] Départ base vide, démo clairement marquée et suppression à double confirmation.
- [ ] Créer activité personnalisée, contact, yaourt, deux lots avec coûts et péremptions.
- [ ] Brouillon : aucune variation de stock ni des KPI validés.
- [ ] Valider une vente avec deux lignes de lots et vérifier le calcul et la décrémentation.
- [ ] Tester stock insuffisant, lot produit dans le futur et lot périmé : validation refusée.
- [ ] Ajouter quantité perdue ; vérifier que le disponible baisse et qu’une perte ne peut pas prendre le stock vendu.
- [ ] Modifier le statut validé/brouillon/annulé et vérifier l’effet métier correspondant.
- [ ] Ajouter deux encaissements partiels ; vérifier reste dû, refus de date antérieure, refus de montant négatif et dépassement.
- [ ] Dossier immobilier : vérifier base × commission × part ; statut validé acquiert la commission, versement incrémente les encaissements.
- [ ] Dépense brouillon exclue du flux ; dépense validée considérée payée.
- [ ] Refus de supprimer tout objet encore référencé.

### Interface et appareils

- [ ] Chrome et Edge sur Windows ; largeur tablette et téléphone 390 px.
- [ ] Naviguer au clavier, vérifier focus, Échap, tabulation dans le dialogue et libellés des champs.
- [ ] Clair/sombre, français/malagasy, persistance après fermeture/réouverture.
- [ ] Recherche, chaque filtre, tri, filtres mémorisés, pagination et export de toutes les lignes filtrées.
- [ ] Exporter CSV dans Excel : UTF-8, séparateur, accent, protection de cellules de formule.
- [ ] Imprimer une vente, une commission, une liste et un état en PDF.
- [ ] Importer sauvegarde valide : confirmation et récupération possible ; mauvais JSON, mauvaise version, lien cassé et fichier >10 Mo : erreur claire.
- [ ] Vider/quota désactivé IndexedDB ou ouvrir en navigation privée : erreur de stockage compréhensible.
- [ ] Bloquer le réseau puis recharger : toutes les fonctions locales restent disponibles.

### Android (non exécuté ici)

- [ ] Android Studio stable + JDK21 ; Sync, puis `assembleDebug`.
- [ ] Installer APK sur appareil API23/Android6 et Android récent avec WebView à jour.
- [ ] Retour Android depuis dialogue, sous-page, tableau de bord et écran initial.
- [ ] Export JSON/CSV par sélecteur système ; importer JSON depuis stockage Android.
- [ ] PDF via impression système ; enregistrer dans Documents.
- [ ] Activer rappel : vérifier permission Android13+, notification, annulation et restrictions batterie. Redémarrer : confirmer que le rappel est perdu et expliquer la réactivation.
- [ ] Mode avion, stockage persistant après arrêt forcé, mise à jour APK avec même signature.
- [ ] Vérifier aucune permission de stockage/caméra/micro/localisation demandée.

### GitHub Actions

- [ ] Déposer toutes les sources à la racine du dépôt, garder `.github/workflows/android.yml`.
- [ ] Pousser main ; vérifier tests, web build, `cap sync`, Gradle `assembleDebug`.
- [ ] Lancer manuellement `workflow_dispatch`.
- [ ] Télécharger `Biznako-1.0.0-debug-Android6-API23` et installer APK sur téléphone.
- [ ] Confirmer que le job échoue si APK absent.


## Changement de synchronisation Cloud (2026-11-08)

Le nouveau client Offline-First AppForge Cloud et l’authentification e-mail/mot de passe ont été ajoutés après la campagne de tests précédente. Les tests locaux listés ci-dessus ne valident pas le Worker, le déploiement, la synchronisation réelle, les conflits, les quotas ni les parcours multiutilisateur/multiappareil. Ces vérifications restent à exécuter après le déploiement du Worker et de D1.
