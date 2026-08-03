# Librairie VideoFeed de Digiteka

[![en](https://img.shields.io/badge/lang-en-red.svg)](README.md)
[![fr](https://img.shields.io/badge/lang-fr-blue.svg)](README.FR.md)

La librairie VideoFeed de Digiteka fournit des composants d'affichage de vidéos :
- un carousel d'aperçus
- un lecteur vertical de vidéos, avec navigation par scroll et swipe

# Installation

Ajouter la dépendance à votre fichier `build.gradle`:

``` kotlin    
dependencies {    
  implementation("com.github.digiteka:videofeed-android:2.1.1")  
}   
```

Et ajouter le token d'accès jitpack à votre projet dans votre fichier `settings.gradle.kt` :

``` kotlin
dependencyResolutionManagement {
    maven{
        url = uri("https://jitpack.io")
        credentials {
            username = "your_access_key_here"
        }
    }
}
```

# Utilisation

Vous devez disposer d'une clé MDTK pour utiliser la librairie.
En option, un chemin d'AdUnit peut aussi être renseigné au format `/{networkCode}/{adBlockPath}`.
De plus, un identifiant de zone (zone ID) peut être fourni pour personnaliser le contenu du feed.

## Carousel

Le carousel d'aperçus est un composant d'affichage de vignettes de vidéos en mode horizontal.
Il peut être intégré via un composant XML ou Compose.

### XML

``` xml
<com.digiteka.videofeed.ui.carousel.VideoFeedCarousel
    android:id="@+id/videoFeedCarousel"
    android:layout_width="match_parent"
    android:layout_height="@dimen/carousel_height" />
```

Le composant doit ensuite être chargé via la méthode `load()` prenant en paramètre votre clé MDTK et les paramètres optionnels :

``` kotlin
binding.videoFeedCarousel.load(mdtk, adUnitPath, zoneId)
```

Par défaut, le clic sur une vidéo ouvre le lecteur vidéo (VideoFeed) en plein écran dans une nouvelle activity.
Il est possible de surcharger ce comportement en ajoutant un listener :

``` kotlin
binding.videoFeedCarousel.onVideoClicked = { mdtk, videoId ->
  // Handle video click
}
```

### Compose

``` kotlin
VideoFeedCarousel(
    modifier = Modifier
        .fillMaxWidth()
        .height(AppTheme.dimens.carouselHeight),
    mdtk = mdtk,
    adUnitPath = adUnitPath,
    zoneId = zoneId,
    onVideoClicked = { mdtk, videoId ->
      // Handle video click
    }
)
```

Le paramètre `onVideoClicked` est optionnel. S'il n'est pas renseigné, le clic sur une vidéo ouvrira le lecteur vidéo (VideoFeed) en plein écran dans une nouvelle activity.

## VideoFeed

Le VideoFeed est un lecteur vidéo vertical, avec navigation par scroll et swipe.
Il peut être intégré via une activity ou un fragment.

### Activity

``` kotlin
startActivity(VideoFeedActivity.newInstance(context, mdtk, videoId, adUnitPath, zoneId))
```

La paramètre `videoId` est nullable. Si renseigné, le lecteur vidéo s'ouvrira directement sur la vidéo correspondante ; sinon, le lecteur s'ouvrira sur la première vidéo du feed.

### Fragment

`VideoFeedFragment` est un fragment AndroidX et peut être intégré comme tout autre fragment.
Dans l'exemple suivant, il est inséré avec un `FragmentContainerView` dans un layout XML.

``` xml
<androidx.fragment.app.FragmentContainerView
    android:id="@+id/defaultUiActivityContainerView"
    android:name="com.digiteka.videofeed.ui.VideoFeedFragment"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />   
```

Il doit ensuite être initialisé avec la clé MDTK :

``` kotlin
videoFeedFragment.load(mdtk, videoId, adUnitPath, zoneId, onCloseClicked)
```

La paramètre `videoId` est nullable. Si renseigné, le lecteur vidéo s'ouvrira directement sur la vidéo correspondante ; sinon, le lecteur s'ouvrira sur la première vidéo du feed.

## Configuration avancée

### Ouvrir le videoFeed depuis un lecteur iframe

1. Créer une interface JavaScript / Android qui intercepte les messages POST envoyés par le lecteur iframe et lance le videoFeed si un message de type `trigger_vf_chromeless` est reconnu :
``` kotlin
class AndroidJavascriptInterface(
    private val context: Context,
    private val mdtk: String,
    private val adUnitPath: String?,
    private val zoneId: String?
) {
    @JavascriptInterface
    fun postMessage(data: String) {
        if (data.contains("trigger_vf_chromeless")) {
            data.split("-").lastOrNull()?.let { videoId ->
                context.startActivity(VideoFeedActivity.newInstance(context, mdtk, videoId, adUnitPath, zoneId))
            }
        }
    }
}
```

2. Ajouter l'interface à la WebView :
``` kotlin
webview.addJavascriptInterface(AndroidJavascriptInterface(context, mdtk, adUnitPath, zoneId), "AndroidJavascriptInterface")
```

3. Envoyer les messages POST JavaScript à l'interface Android en intégrant le script suivant dans la page HTML :
``` html
<script>
  window.addEventListener('message', function(e) {
      AndroidJavascriptInterface.postMessage(e.data);
  });
</script>
```

Le script peut aussi être injecté à la volée via un `webviewClient` :
``` kotlin
webview.webViewClient = object: WebViewClient() {
  override fun onPageFinished(view: WebView?, url: String?) {
    super.onPageFinished(view, url)
    view?.loadUrl("javascript:window.addEventListener('message', function(e) {AndroidJavascriptInterface.postMessage(e.data);});")
  }
}
```

### Interception d'URL

Vous pouvez intercepter les URLs cliquées dans le VideoFeed en configurant le gestionnaire d'URL global :

``` kotlin
VideoFeedConfig.shouldOverrideUrlLoading = { uri ->
    // Retourner true si vous avez géré l'URL, false sinon
    when {
        uri.host == "example.com" -> {
            // Gérer des URLs spécifiques
            true
        }
        else -> false // Laisser le navigateur par défaut s'en charger
    }
}
```

### Gestion du bouton fermer

Lors de l'utilisation de `VideoFeedFragment`, vous pouvez contrôler la visibilité et le comportement du bouton fermer en passant le paramètre `onCloseClicked` à la méthode `load()` :

``` kotlin
// Avec bouton fermer
videoFeedFragment.load(mdtk, videoId, adUnitPath, zoneId) {
    // Gérer le clic sur le bouton fermer
    finish() // ou toute logique personnalisée
}

// Sans bouton fermer
videoFeedFragment.load(mdtk, videoId, adUnitPath, zoneId, null)
```

Le bouton fermer n'apparaîtra que si le paramètre `onCloseClicked` n'est pas `null`. La `VideoFeedActivity` gère automatiquement le bouton fermer en terminant l'activity.