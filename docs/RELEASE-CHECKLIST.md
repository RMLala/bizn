# Avant une distribution durable

- Remplacer `mg.biznako.gestion` par un identifiant de domaine contrôlé avant la première release publique, dans les emplacements Android indiqués dans README.
- Décider juridiquement du contenu d’un reçu/facture local, règles TVA, numérotation réglementaire et obligations de conservation à Madagascar. Les documents actuels sont des relevés internes.
- Valider les méthodes de stock et de pertes avec les responsables de production ; coûts historiques des lots sont éditables.
- Mettre en place une politique de sauvegarde : fréquence, lieu distinct et vérification périodique de restauration JSON.
- Faire une recette Android physique, installation/mise à jour signée, appareils API23 et appareils récents. Mesurer le comportement du WebView d’entrée de gamme.
- Conserver keystore release hors du projet, contrôler l’accès, enregistrer et sauvegarder les mots de passe séparément. Tester une mise à jour release avant diffusion.
- Si Drive/Gemini sont activés, construire le backend séparément conformément à CLOUD-SECURITE.md, faire revoir l’authentification, les quotas, la confidentialité et la gestion des conflits.
- Établir procédure de migration du schéma IndexedDB ; une version incompatible ne doit jamais réinitialiser silencieusement l’ancienne base.
