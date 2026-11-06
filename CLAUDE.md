# Contexte du projet (résumé de la conversation précédente)

L'utilisateur est francophone, pas développeur, et suit la conversation sur iPhone. Réponds en français, en le tutoyant, simplement et sans jargon.

## La voiture et l'autoradio
- Dacia Sandero 2012-2017. Autoradio SXAUTO / BXLIYER 9" 2 Din, Android annoncé 15 (probablement une version maquillée : minSdk 24 par prudence), 2 Go RAM + 64 Go, DSP intégré, caméra de recul, DAB, boutons sur le bord gauche (MIC, RST, POWER, HOME, BACK, VOL±), mode « Fast-Boot » (veille au lieu d'extinction).
- **CarPlay uniquement sans fil** (le CarPlay en USB ne marche pas sur ce modèle), via une appli de l'autoradio (sans doute ZLINK). L'utilisateur a un iPhone.
- Résolution d'écran **non confirmée** (supposée 1280×720). Photo de « Paramètres → À propos » demandée, pas encore reçue.

## L'appli « Verre » (ce dépôt)
- Écran d'accueil Android (launcher) en Java, **sans Gradle** : `./build.sh` → `build/Verre.apk` (~130 Ko, version 2.0). APK à installer : `release/Verre.apk`.
- Lien de téléchargement direct (dépôt public) : `https://github.com/26dwh7yy8g-rgb/Carplay/raw/main/release/Verre.apk`. Le lien est trop long à taper sur l'autoradio : l'utilisateur crée un lien court lui-même sur tinyurl.com (la création de lien court par Claude a été bloquée dans la session), ou passe par une clé USB.
- Fonctions : jour/date/heure, musique en cours (MediaSession via accès aux notifications, boutons qui marchent même sans), tuile CarPlay + « Ouverture auto » au démarrage (écran allumé), curseurs verticaux volume / luminosité (WRITE_SETTINGS), boutons Nuit / Muet / Caméra / Veille, dock (Radio, Musique, Téléphone, Son, Caméra, Réglages, Applis), écran Applis avec recherche + section **Streaming**, **vidéos bloquées quand le GPS dit que la voiture roule** (> ~7 km/h ; sinon demande de confirmer l'arrêt). Appui long = choisir l'appli de chaque bouton (`Roles.java` devine par mots-clés). Toucher l'horloge = menu Personnaliser.
- Architecture : tout est dessiné en « unités maquette » 1280×720 mises à l'échelle (`Stage`, `Abs`, `U.s`). Chaque widget = une seule View Canvas (`BaseCard`).
- **Liquid Glass** : `Backdrop` réduit la pochette en cours (ou la photo choisie, `Photos`) à 128×72 et la floute UNE fois par changement ; `Glass` redessine sous chaque panneau la portion de fond qui est derrière (réfraction GPU via `GlassShader`, portage AGSL du shader de dashersw/liquid-glass-js, Android 13+ ; sinon loupe 1,06 + voile + reflet). Fond au choix (pochette / ma photo / noir) ; en noir = widgets pleins façon Apple sombre. Police Inter réduite (assets/fonts), licences dans assets/licenses.

### Construire dans une session cloud
- Maven Central renvoie 429 dans l'environnement cloud, donc pas de Gradle. `dl.google.com` est accessible.
- SDK : télécharger les commandlinetools depuis dl.google.com, puis `sdkmanager "platforms;android-34" "build-tools;35.0.0"`. Le d8 de build-tools 34 plante avec le JDK 21 : utiliser 35.
- Lint : `cmdline-tools/latest/bin/lint --sources app/src --resources app/res --classpath build/classes --libraries <sdk>/platforms/android-34/android.jar app`.
- **Clé de signature hors du dépôt** (dépôt public ; un push contenant la clé a été bloqué). `build.sh` en crée une dans `keystore/` (ignoré par git). Une nouvelle clé = l'utilisateur doit **désinstaller l'ancienne Verre** avant d'installer la nouvelle : le prévenir.

## Le design
- Maquettes (artifact privé de l'utilisateur) : https://claude.ai/artifact/AYSfbHRvLbdGMMdrfNK2fP. Styles A Onyx & Champagne, B Ambiance lumineuse, C Cuir & Cuivre, D Verre dépoli (accueil + écran Applis).
- Historique : D (verre dépoli) jugé « trop IA » → maquette « deux cadrans » (skill design-taste-frontend) refusée → **style Apple** (widgets sombres, Inter, couleurs système) validé → **Liquid Glass** demandé et validé (fond = pochette floutée, widgets en verre), refait avec le skill liquid-glass-js (maquette WebGL) puis porté en natif (v2.0).
- Mise en page actuelle (unités 1280×720, marges 24, écarts 16) : musique (24,24,608×536), heure (648,24,296×260), CarPlay (960,24,296×260), curseurs (648,300,296×260), raccourcis (960,300,296×260), dock (24,576,1232×120).
- Règles : pas de copie de l'interface Apple ni d'une marque de voiture, pas de logos. Garder toutes les fonctions et la légèreté.

## Sujets en attente
1. **Latence CarPlay** (sans fil). Conseils déjà donnés : Wi-Fi 5 GHz, résolution / images par seconde dans l'appli CarPlay, redémarrage avec RST, oublier les autres réseaux Wi-Fi de l'autoradio, nettoyer le Bluetooth, animations à 0,5x dans les options développeur, firmware uniquement auprès du vendeur. Photo des réglages de l'appli CarPlay demandée, pas reçue.
2. **Bug « écran d'ordinateur Windows »** qui s'affiche parfois à la place de CarPlay, **en roulant comme à l'arrêt**, donc probablement interne (écran de repli de l'appli CarPlay ou appli de recopie d'écran / Miracast). Demandé : photo, nom de l'appli via le bouton des applis récentes, à quel moment ça arrive, comment ça s'en va. Conseillé : désactiver la recopie d'écran / Miracast.
3. **Réglage DSP** pour tirer le maximum des enceintes. À recevoir : photos de chaque écran du DSP, enceintes d'origine ou non (marque, taille, tweeters, arrière, caisson, ampli), distances tête du conducteur → centre de chaque enceinte en cm (pour calculer les retards), style de musique, son centré sur le conducteur ou pour tous. Plan : alignement temporel, filtres passe-haut, égaliseur surtout en coupant.
4. **Streaming** (déjà expliqué) : les applis audio de l'iPhone apparaissent dans CarPlay ; Apple bloque la vidéo dans CarPlay ; la recopie d'écran de l'iPhone donne un écran noir sur Netflix / Disney+ / Prime / Canal+ ; le mieux est d'installer les applis Android sur l'autoradio (section Streaming de Verre). Vidéo en roulant interdite (jusqu'à 1 500 € d'amende et 3 points).
