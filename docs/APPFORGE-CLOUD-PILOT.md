# Pilote AppForge Cloud pour Biznako

Biznako est le premier client pilote du socle AppForge Cloud. IndexedDB reste la base de travail locale. Aucun compte Cloud n’est nécessaire pour continuer à utiliser l’application hors ligne.

## Préparer la connexion

- Déployer le Worker et la migration D1 dédiés en suivant `appforge-cloud/README.md`.
- Ajouter l’origine HTTPS de Biznako à `APP_ORIGINS`.
- Dans Réglages → AppForge Cloud, l’adresse HTTPS du Worker est intégrée au build Biznako. Le client ne la saisit pas. Pour changer de domaine, le développeur modifie la constante de configuration dans `src/app.js` puis republie le build.
- Créer le compte propriétaire initial une seule fois avec le secret de bootstrap, puis retirer ce secret de Cloudflare. Les membres ultérieurs reçoivent une invitation du propriétaire/admin.
- Connexion e-mail/mot de passe. Google OAuth n’est pas requis. Une invitation n’est pas envoyée par e-mail : transmettre le code à la bonne adresse. Les comptes existants acceptent leur invitation après connexion; les nouveaux la saisissent lors de la création du compte.

Un fichier `file://` ou une session sans Internet continue à fonctionner localement, mais ne peut pas joindre le Worker. Pour la synchronisation Web, utiliser HTTPS. L’APK Capacitor utilise l’API HTTPS du Worker; il n’a pas besoin de Google OAuth.

## Synchronisation automatique

- Les enregistrements sont d’abord sauvegardés sur l’appareil. Une session active lance la synchronisation après modification, au lancement en ligne, au retour au premier plan et à la reconnexion.
- Au premier accès à un Cloud déjà peuplé, Biznako demande quelle copie conserver. Choisir l’appareil provoque le remplacement explicite du snapshot Cloud après confirmation; choisir Cloud charge ce snapshot localement.
- Ensuite, l’app fusionne les changements par ID stable et utilise une révision D1. Les modifications concurrentes du même enregistrement s’arrêtent et demandent de choisir les versions locales ou Cloud. Aucune fusion silencieuse.
- Les suppressions sont transmises comme changements lorsque la base connue permet de les distinguer. Une sauvegarde JSON locale reste disponible.
- Une session expirée doit être renouvelée en se reconnectant. La panne, la révocation ou l’absence de réseau suspend la synchronisation, sans bloquer la saisie locale.

La session (jeton bearer de 30 jours) est conservée dans IndexedDB non chiffré afin de rétablir la synchronisation entre lancements; utiliser la déconnexion sur un appareil partagé. D1 conserve les snapshots JSON sans chiffrement de bout en bout. Google Drive reste une sauvegarde/export facultatif séparé.
