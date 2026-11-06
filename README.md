# Verre — écran d'accueil « Liquid Glass » pour autoradio Android

Écran d'accueil façon Apple, avec un effet de verre, pour autoradio Android. Il a été conçu pour un SXAUTO / BXLIYER 9" de Dacia Sandero 2012-2017.
Léger : **environ 130 Ko**, sans pub ni pistage, et rien ne tourne en arrière-plan.

## Ce qu'il y a dedans

- **Liquid Glass** : le fond prend les couleurs floutées de la pochette du morceau en cours. Les widgets sont en verre et dévient la lumière sur leurs bords.
  - Le flou est calculé **une fois par morceau** (sur une image de 128×72), jamais en continu.
  - Sur **Android 13 et plus**, la réfraction est calculée par la carte graphique. C'est un portage du shader de [liquid-glass-js](https://github.com/dashersw/liquid-glass-js).
  - Sur les versions plus anciennes, l'effet est plus simple : une loupe légère et un reflet.
- **Fond au choix** (toucher l'horloge → Fond d'écran) : la pochette en cours, une photo de ta galerie, ou du noir. En noir, ce sont des widgets pleins façon Apple en mode sombre.
- **Accueil** :
  - le grand widget musique (pochette, titre, progression, boutons) ;
  - le jour, la date et l'heure ;
  - le bouton **CarPlay**, avec l'interrupteur « Ouverture auto » au démarrage ;
  - les curseurs verticaux **volume** et **luminosité** ;
  - les boutons **Nuit / Muet / Caméra / Veille** ;
  - un dock d'icônes.
- **Écran Applis** : recherche, section **Streaming** (Netflix, YouTube, Disney+, Spotify, Deezer…, si ces applis sont installées sur l'autoradio), toutes les applis.
- **Vidéo seulement à l'arrêt** : les applis vidéo ne s'ouvrent pas si le GPS indique que la voiture roule (au-delà d'environ 7 km/h).
- Police **Inter**, réduite aux caractères latins.

## Installation

1. Sur l'autoradio, connecte-toi à internet (partage de connexion de l'iPhone).
2. Ouvre le navigateur et télécharge :
   `https://github.com/26dwh7yy8g-rgb/Carplay/raw/main/release/Verre.apk`
   Tu peux aussi copier `release/Verre.apk` sur une clé USB et l'ouvrir avec le gestionnaire de fichiers.
3. Ouvre le fichier et accepte « Installer des applis inconnues » si l'autoradio le demande.
4. Appuie sur le bouton Accueil, puis choisis **Verre** et **Toujours**.
5. Au premier lancement, la fenêtre **Configurer Verre** propose 4 autorisations. Elles sont toutes facultatives.

Pour revenir à l'ancien écran d'accueil : Réglages Android > Applis > Applis par défaut > Écran d'accueil.

## Utilisation

| Geste | Effet |
|---|---|
| Toucher l'horloge | Menu Personnaliser (fond d'écran, configuration, appli CarPlay…) |
| Appui long sur une icône du dock ou sur CarPlay | Choisir l'appli à ouvrir (les noms varient selon l'autoradio) |
| Glisser sur un curseur vers le haut ou le bas | Volume / luminosité |
| Appui long sur une appli dans « Applis » | Infos / désinstaller |
| Toucher la musique | Ouvre l'appli qui joue, ou demande l'accès pour afficher le titre |

## Limites connues

- Le titre et la pochette s'affichent pour ce qu'Android voit passer (Bluetooth, radio, applis). Pour la musique jouée **dans CarPlay**, ça dépend de l'appli CarPlay de l'autoradio.
- Luminosité : certains autoradios gèrent l'éclairage autrement. Le curseur peut alors ne rien changer.
- « Veille » affiche un écran noir avec le rétroéclairage au minimum. Toucher l'écran pour revenir.

## Construire l'APK

Il faut un JDK 17 ou plus et le SDK Android (`platforms;android-34`, `build-tools;35.0.0`). Gradle n'est pas nécessaire :

```sh
ANDROID_HOME=/chemin/vers/android-sdk ./build.sh   # -> build/Verre.apk
```

La clé de signature n'est **pas** dans le dépôt (`keystore/` est ignoré). Le premier build en crée une localement. Un APK signé avec une autre clé ne s'installe pas par-dessus l'ancien : il faut désinstaller Verre avant d'installer une version construite ailleurs.

## Crédits

- Réfraction : d'après le shader de [liquid-glass-js](https://github.com/dashersw/liquid-glass-js), licence MIT, © 2025 Armagan Amcalar (`app/assets/licenses/liquid-glass-js-MIT.txt`).
- Police Inter : SIL Open Font License 1.1, © The Inter Project Authors (`app/assets/licenses/Inter-OFL.txt`).
