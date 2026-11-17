# Biznako — migration sauvegarde V3

## Constats vérifiés sur main
- `src/index.html` exporte `JSON.stringify(state,null,2)` via la fonction `backup()` : sauvegarde en clair.
- Le formulaire de restauration accepte des fichiers JSON ; préserver la compatibilité des anciens exports.
- `README.md` décrit Google OAuth comme nécessaire, tandis que le HTML embarque `https://accounts.google.com/gsi/client` ; déterminer si ce script sert à la connexion Cloud avant retrait.
- Stockage local IndexedDB et file d'attente Cloud présents : ne pas toucher à la base lors de la migration.

## Correctif fonctionnel à implémenter dans une PR distincte
1. Export JSON compatible + option chiffrée (AES-GCM, dérivation de clé robuste et sel aléatoire), sans stocker le secret en clair.
2. Partage natif Android via Capacitor Share / Filesystem ; import depuis le sélecteur système, Drive compris ; export/import fichier HTML.
3. Restauration avec validation de schéma, aperçu, confirmation explicite et sauvegarde de sécurité ; aucune écriture si validation échoue.
4. Vérifier puis retirer uniquement les dépendances OAuth Drive réellement inutilisées, sans casser l'authentification Cloud.
5. Tests aller-retour JSON ancien, chiffré, mauvais mot de passe, données corrompues, hors ligne, Android 6/10.

## Statut
Audit documenté seulement. Aucun changement de données ni correction fonctionnelle revendiquée.