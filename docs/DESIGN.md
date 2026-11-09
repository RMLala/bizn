# Système visuel et interface

Palette claire : fond #F4F6F3, surfaces #FFFFFF, texte #182E29, secondaire #63746E, vert principal #17624F, surface accent #E4F0E9, bordure #DDE5DF, alerte #A45D0A, erreur #B53838.
Palette sombre : fond #111C19, surface #1C2C26, texte #EEF7F0, secondaire #B1C2B8, accent #77CDAA, bordure #35483E, alerte #F5BC69, erreur #FF9999.
Typographie système locale : system-ui, -apple-system, Segoe UI, sans-serif. Pas de téléchargement de police. Icônes de navigation typographiques locales et monogramme b géométrique Android, sans Lucide/CDN obligatoire.

Espacements : base 4 px, contrôles 10–15 px, cartes 16–22 px, contenu 16/24/36 px selon largeur. Rayons 10–11 px pour contrôles, 14–20 px pour cartes. Navigation latérale 240 px desktop, 210 px intermédiaire, tiroir mobile 260 px. Point mobile 720 px, tablette 1100 px. KPI 4 colonnes desktop, 2 colonnes téléphone ; analyses empilées. Tableaux en défilement horizontal dans leur conteneur, sans dépendance au survol.

Composants : bandeau de contexte, filtres, cartes KPI avec accès au détail, barres de contribution, listes paginées, badges de statut, menu d’actions, dialogue de saisie, lignes répétables de vente, notification de résultat, guide numéroté et documents imprimables.

États : chargement initial textuel, base vide avec action d’ajout, absence de résultats distincte, erreur de validation dans le formulaire, erreur de persistance explicite, boutons de pagination indisponibles désactivés. Les commandes Android absentes du HTML sont désactivées et expliquées. Le vert distingue les éléments actifs ; un libellé accompagne les statuts pour ne pas dépendre seulement de la couleur.

Accessibilité : boutons tactiles 44 px (navigation 40 px minimum), labels enveloppants, focus visible #D79739, rôle dialog avec titre, fermeture Échap, boucle Tab du dialogue, rôle status pour notifications. Animations désactivées avec prefers-reduced-motion. Thème et langue persistants. Les sélecteurs de date natifs suivent la langue de l’OS ; tableaux et documents affichent jj/mm/aaaa.

Les données saisies sont échappées avant insertion HTML. Les montants ne sont jamais décoratifs : ils proviennent du modèle métier partagé avec les tests. Limite : revue visuelle et essais ciblés, pas certification WCAG ni audit de sécurité indépendant.
