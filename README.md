# Verre — écran d'accueil pour autoradio Android

Écran d'accueil « verre dépoli » pour autoradio Android (testé pour un modèle SXAUTO / BXLIYER 9" de Dacia Sandero 2012-2017).
Très léger : **60 Ko**, aucune pub, aucun pistage, rien ne tourne en arrière-plan.

## Ce qu'il y a dedans

- **Accueil** : horloge + semaine, musique en cours (pochette, progression, boutons), grosse tuile **CarPlay**, curseurs **volume** et **luminosité**, boutons **Nuit / Muet / Caméra / Écran off**, dock d'applis en couleur.
- **Ouverture auto de CarPlay** au démarrage de la voiture (interrupteur sur la tuile CarPlay).
- **Écran Applis** : recherche, section **Streaming** (Netflix, YouTube, Disney+, Spotify, Deezer… s'ils sont installés sur l'autoradio), toutes les applis.
- **Vidéo seulement à l'arrêt** : les applis vidéo ne s'ouvrent pas si le GPS indique que la voiture roule (plus de ~7 km/h).
- 3 fonds : Aurore, Océan, Braise.

## Installation

1. Sur l'autoradio, connecte-toi à internet (partage de connexion de l'iPhone).
2. Ouvre le navigateur et télécharge :
   `https://github.com/26dwh7yy8g-rgb/Carplay/raw/main/release/Verre.apk`
   (ou copie `release/Verre.apk` sur une clé USB et ouvre-le avec le gestionnaire de fichiers).
3. Ouvre le fichier et accepte « Installer des applis inconnues » si demandé.
4. Appuie sur le bouton Accueil et choisis **Verre** puis **Toujours**.
5. Au premier lancement, la fenêtre **Configurer Verre** propose 4 autorisations (toutes facultatives).

Pour revenir à l'ancien écran d'accueil : Réglages Android > Applis > Applis par défaut > Écran d'accueil.

## Utilisation

| Geste | Effet |
|---|---|
| Toucher l'horloge | Menu Personnaliser (fond, configuration, appli CarPlay…) |
| Appui long sur une icône du dock ou sur CarPlay | Choisir l'appli à ouvrir (les noms varient selon l'autoradio) |
| Appui long sur une appli dans « Applis » | Infos / désinstaller |
| Toucher la musique | Ouvre l'appli qui joue (ou demande l'accès pour afficher le titre) |

## Limites connues

- Le titre et la pochette s'affichent pour ce qu'Android voit passer (Bluetooth, radio, applis). La musique jouée **dans CarPlay** dépend de l'appli CarPlay de l'autoradio.
- Luminosité : certains autoradios gèrent l'éclairage autrement, le curseur peut alors ne rien changer.
- « Écran off » affiche un écran noir avec le rétroéclairage au minimum (toucher pour revenir).

## Construire l'APK

Il faut un JDK 17+ et le SDK Android (`platforms;android-34`, `build-tools;35.0.0`), sans Gradle :

```sh
ANDROID_HOME=/chemin/vers/android-sdk ./build.sh   # -> build/Verre.apk
```

La clé de signature n'est **pas** dans le dépôt (`keystore/` est ignoré). Le premier build en crée une localement. Un APK signé avec une autre clé ne s'installe pas par-dessus l'ancien : désinstalle Verre avant d'installer une version construite ailleurs.
