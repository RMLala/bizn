# AppForge Cloud et sécurité — état du pilote Biznako

## Ce qui est préparé

Le Worker Cloudflare AppForge Cloud et la migration D1 séparée sont dans `appforge-cloud/`. Ils prennent en charge les comptes par e-mail/mot de passe, les organisations, rôles, invitations, sessions révocables, snapshots et contrôle de révision. Google OAuth n’est pas requis pour l’authentification ou la synchronisation.

Biznako a un client de synchronisation automatique Offline-First pour le Web HTTPS et l’APK Capacitor : écriture locale d’abord, transmission après modification et au retour réseau. Le fichier `file://` et le mode hors ligne restent utilisables localement. Le backend n’est pas encore déployé avec une base D1 et des origines autorisées; le fonctionnement Cloud ne doit pas être annoncé comme vérifié avant des essais multiutilisateur et multiappareil.

## Architecture

Biznako (IndexedDB local) → HTTPS → Worker AppForge Cloud → D1 par organisation. Les droits sont vérifiés par appartenance active et rôle. La synchronisation possède une révision globale par organisation/application et fusionne les modifications par ID. Un conflit de même enregistrement exige un choix. La session de 30 jours est conservée côté appareil dans IndexedDB non chiffré pour reprendre au lancement; le bouton de déconnexion révoque la session serveur. L’accès local n’est pas bloqué par une session expirée ou une panne cloud.

D1 stocke le snapshot JSON en clair au niveau applicatif. HTTPS protège le transport; il n’y a pas de chiffrement de bout en bout. L’administrateur du service peut techniquement accéder aux données. Éviter d’y stocker des secrets d’API ou données qui n’ont pas besoin d’être synchronisées. Le snapshot est limité à 500 Ko.

## Identité, invitations et réinitialisation

- Le premier compte propriétaire est créé avec le secret Cloudflare `BOOTSTRAP_SECRET`. Supprimer ce secret immédiatement après le bootstrap réussi.
- Les nouveaux membres rejoignent une organisation par invitation limitée à leur adresse et à sept jours. Le code d’invitation est transmis manuellement; aucun e-mail automatique n’est configuré.
- L’API limite les échecs de connexion à cinq par e-mail/IP par fenêtre de 15 minutes. Les mots de passe ont 12 caractères minimum et sont dérivés avec PBKDF2-SHA-256 et sel aléatoire. Les jetons de session bruts ne sont pas stockés dans D1.
- La réinitialisation est émise par un owner/admin, valable 30 minutes, et transmise manuellement. Les sessions actives sont révoquées après changement de mot de passe.
- La liste CORS `APP_ORIGINS` doit contenir seulement les origines exactes utilisées. Capacitor peut envoyer `https://localhost`; le vérifier depuis la version réellement distribuée avant de l’autoriser.

## Synchronisation prudente

La première synchronisation exige un choix explicite s’il existe déjà des données Cloud. Les écritures utilisent `expectedRevision`, et le Worker renvoie HTTP 409 si une autre version a été enregistrée. En cas de divergence sur un même élément, l’application ne rapporte pas de réussite : l’utilisateur choisit si la version locale ou Cloud est conservée. Les autres ajouts, changements et suppressions compatibles sont fusionnés par IDs stables.

Le format actuel reste un snapshot complet; il convient au pilote Biznako et à son plafond de 500 Ko. Pour un usage avec gros volumes, pièces jointes ou forte simultanéité, faire évoluer vers opérations idempotentes, versions par enregistrement, tombstones dédiés, pagination et fichiers dans R2. Ajouter sauvegardes D1, audit, limites d’usage, alertes et politique de suppression avant la commercialisation.

## Google Drive et Gemini

Google Drive est une sauvegarde/export distincte et facultative, activée uniquement avec consentement et OAuth dédiés. Ce pilote ne l’active pas. Gemini reste un autre service, facultatif; toute clé doit rester côté serveur, derrière quotas, autorisation et consentement. Aucun identifiant Google ou secret n’est requis pour AppForge Cloud.
