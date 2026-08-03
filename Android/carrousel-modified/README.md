#Digiteka VideoFeed Library

[![en](https://img.shields.io/badge/lang-en-red.svg)](README.md)
[![fr](https://img.shields.io/badge/lang-fr-blue.svg)](README.FR.md)

The Digiteka VideoFeed library provides video display components:
- a carousel of video previews
- a vertical video player, with scroll and swipe navigation

# Installation

Add the dependency to your `build.gradle` file:

``` kotlin    
dependencies {    
  implementation("com.github.digiteka:videofeed-android:2.1.1")
}   
```

Then add the jitpack access token to your project in your `settings.gradle.kt` file:

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

# Usage

You need an MDTK key to use the library.
Optionally, an AdUnit path can also be specified in the format `/{networkCode}/{adBlockPath}`.
Additionally, a zone ID can be provided to customize the feed content.

## Carousel

The carousel is a component for displaying video thumbnails in horizontal mode. It can be integrated via XML or Compose component.

### XML

``` xml
<com.digiteka.videofeed.ui.carousel.VideoFeedCarousel
    android:id="@+id/videoFeedCarousel"
    android:layout_width="match_parent"
    android:layout_height="@dimen/carousel_height" />
```

The component must then be loaded via the `load()` method taking your MDTK key and optional parameters:

``` kotlin
binding.videoFeedCarousel.load(mdtk, adUnitPath, zoneId)
```

By default, clicking on a video opens the video player (VideoFeed) in full screen in a new activity.
It is possible to override this behavior by adding a listener:

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

The `onVideoClicked` parameter is optional.
If not provided, clicking on a video will open the video player (VideoFeed) in full screen in a new activity.

## VideoFeed

The VideoFeed is a vertical video player, with scroll and swipe navigation. It can be integrated via an activity or a fragment.

### Activity

``` kotlin
startActivity(VideoFeedActivity.newInstance(context, mdtk, videoId, adUnitPath, zoneId))
```

The `videoId` parameter is nullable. If provided, the video player will open directly on the corresponding video; otherwise, the player will open on the first video of the feed.

### Fragment

`VideoFeedFragment` is an AndroidX fragment and can be integrated like any other fragment. In the following example, it is inserted with a `FragmentContainerView` in an XML layout.

``` xml
<androidx.fragment.app.FragmentContainerView
    android:id="@+id/defaultUiActivityContainerView"
    android:name="com.digiteka.videofeed.ui.VideoFeedFragment"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />   
```

It must then be initialized with the MDTK key:

``` kotlin
videoFeedFragment.load(mdtk, videoId, adUnitPath, zoneId, onCloseClicked)
```

The `videoId` parameter is nullable. If provided, the video player will open directly on the corresponding video; otherwise, the player will open on the first video of the feed.

## Advanced Configuration

### Open the VideoFeed from an iframe player

1. Create a JavaScript / Android interface that intercepts POST messages sent by the iframe player and launches the VideoFeed if a `trigger_vf_chromeless` message is recognized:
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

2. Add the interface to the WebView:
``` kotlin
webview.addJavascriptInterface(AndroidJavascriptInterface(context, mdtk, adUnitPath, zoneId), "AndroidJavascriptInterface")
```

3. Send JavaScript POST messages to the Android interface by integrating the following script into the HTML page:
``` html
<script>
  window.addEventListener('message', function(e) {
      AndroidJavascriptInterface.postMessage(e.data);
  });
</script>
```

The script can also be injected on the fly via a `webviewClient`:
``` kotlin
webview.webViewClient = object: WebViewClient() {
  override fun onPageFinished(view: WebView?, url: String?) {
    super.onPageFinished(view, url)
    view?.loadUrl("javascript:window.addEventListener('message', function(e) {AndroidJavascriptInterface.postMessage(e.data);});")
  }
}
```

### URL Interception

You can intercept URLs clicked within the VideoFeed by configuring the global URL handler:

``` kotlin
VideoFeedConfig.shouldOverrideUrlLoading = { uri ->
    // Return true if you handled the URL, false otherwise
    when {
        uri.host == "example.com" -> {
            // Handle specific URLs
            true
        }
        else -> false // Let the default browser handle it
    }
}
```

### Close Button Handling

When using `VideoFeedFragment`, you can control the close button visibility and behavior by passing the `onCloseClicked` parameter to the `load()` method:

``` kotlin
// With close button
videoFeedFragment.load(mdtk, videoId, adUnitPath, zoneId) {
    // Handle close button click
    finish() // or any custom logic
}

// Without close button
videoFeedFragment.load(mdtk, videoId, adUnitPath, zoneId, null)
```

The close button will only appear if the `onCloseClicked` parameter is not `null`. The `VideoFeedActivity` automatically handles the close button by finishing the activity.