# CLAUDE.md — Digiteka SDK Test App

## Architecture

App Android de test pour valider l'intégration de 2 SDK Digiteka :
- **VideoFeed** : carrousel vidéo + lecteur vertical (style TikTok)
- **InStream** : lecteur vidéo flottant en contexte d'article

+ CMP (Google UMP) pour tester la monétisation en cadre RGPD.

**Stack** : Kotlin, Jetpack Compose (+ `AndroidView` pour composants SDK XML), minSdk 24, targetSdk 35, Gradle Kotlin DSL, Navigation Compose (NavHost, 4 destinations).

**Structure fichiers** :
```
app/src/main/java/com/example/digiteka_test/
├── DigitekaTestApp.kt           // Application class (init InStream + UMP)
├── MainActivity.kt              // Compose Activity + NavHost
├── ui/
│   ├── HomeScreen.kt            // 4 gros boutons
│   ├── InstreamArticleScreen.kt // Faux article + InStream player
│   ├── CarrouselScreen.kt       // VideoFeed Carrousel
│   └── CookieManagerScreen.kt   // Gestion cookies debug ultimedia.com
└── consent/
    └── ConsentManager.kt        // Helper Google UMP (init, TC string)
```

**Navigation** :
```kotlin
NavHost(startDestination = "home") {
    composable("home") { HomeScreen(navController) }
    composable("instream") { InstreamArticleScreen() }
    composable("carrousel") { CarrouselScreen() }
    composable("cookies") { CookieManagerScreen() }
    // Pas de destination pour VideoFeed plein écran (Activity externe)
}
```

**Écrans** (HomeScreen : 4 boutons Material 3 pleine largeur, ~120dp) :

1. **InStream Player** → `InstreamArticleScreen` : article Lorem Ipsum scrollable + `MainPlayerView` (AndroidView, ratio 16:9). Player flottant (VisiblePlayer) apparaît quand le MainPlayer sort du viewport — `BOTTOM_END`, `widthPercent=0.5f`, ratio 16:9, margins 12dp.
2. **VideoFeed Carrousel** → `CarrouselScreen` : `VideoFeedCarrousel` (AndroidView, ~280dp), chargé via `carrousel.load(mdtk, adUnitPath=null, zoneId=null)`.
3. **VideoFeed Plein Écran** → lance directement `VideoFeedActivity` (pas de nouvel écran Compose) via `VideoFeedActivity.newInstance(context, mdtk, videoId=null, adUnitPath=null, zoneId=null)`.
4. **Debug Cookies** → `CookieManagerScreen` : gestion des cookies sur `.ultimedia.com` via `android.webkit.CookieManager` (ajouter/lister/supprimer un cookie/tout supprimer), partagés avec les WebViews internes des SDK (même `CookieManager` système). Snackbar de confirmation après chaque action.

## Contraintes métier

**Dépendances SDK (JitPack privé)** — `settings.gradle.kts` : repository JitPack authentifié via `local.properties` (clé `DIGITEKA_JITPACK_ACCESS_KEY`, lue avec `Properties()` / `FileInputStream`).

```kotlin
dependencies {
    implementation("com.github.digiteka:videofeed-android:2.1.1")
    implementation("com.github.digiteka:SDK-instream-Android:1.1.0-0")
    implementation("com.google.android.ump:user-messaging-platform:3.1.0")
}
```

`local.properties` (NE PAS COMMITTER) :
```properties
DIGITEKA_JITPACK_ACCESS_KEY=jp_fuceltdfpdjulq01r2ca4js464
```

**Paramètres SDK** :

| SDK | Paramètre | Valeur |
|-----|-----------|--------|
| InStream | MDTK | `01850262` |
| InStream | zone | `1` |
| InStream | src | `xrkxv0k` |
| InStream | urlReferrer | `https://test-app.digiteka.com/article` |
| InStream | playMode | `PlayMode.VISIBLE_AT_FIFTY_PERCENT` |
| VideoFeed | MDTK | `01573101` |
| VideoFeed | zoneId / adUnitPath | `null` |

**Intégration InStream** (`Application.onCreate`) :
```kotlin
val dtkisConfig = DTKISConfig.Builder(mdtk = "01850262").build()
InStream.initialize(applicationContext = applicationContext, config = dtkisConfig)
```

(dans l'écran article) :
```kotlin
val mainPlayerConfig = DTKISMainPlayerConfig.Builder(
    zone = "1", gdprConsentString = tcString, src = "xrkxv0k",
    urlReferrer = "https://test-app.digiteka.com/article", tagParam = null
).setPlayMode(PlayMode.VISIBLE_AT_FIFTY_PERCENT).build()

val mainPlayerView = remember { MainPlayerView(context) }
val mainPlayerKey = remember { InStream.configureMainPlayer(mainPlayerView, mainPlayerConfig) }

InStream.attachVisiblePlayerTo(
    parent = rootView,
    visiblePlayerConfig = DTKISVisiblePlayerConfig(
        widthPercent = 0.5f, position = Position.BOTTOM_END,
        ratio = "16:9", horizontalMargin = 12f, verticalMargin = 12f
    ),
    mainPlayerKey = mainPlayerKey
)
```

**CMP — Google UMP** : compte AdMob requis (obtenir `APPLICATION_ID`, format `ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY`, à ajouter dans `AndroidManifest.xml` via `<meta-data android:name="com.google.android.gms.ads.APPLICATION_ID">`), message RGPD publié dans AdMob (Confidentialité et messages > Réglementation européenne).

Flux : `requestConsentInfoUpdate()` → `loadAndShowConsentFormIfRequired()` → lire TC String depuis `SharedPreferences` (clé `IABTCF_TCString`).

Debug hors zone EEE : `ConsentDebugSettings.Builder(this).setDebugGeography(DEBUG_GEOGRAPHY_EEA).addTestDeviceHashedId(...)`.

Passage TC String aux SDK : InStream via `gdprConsentString` dans `DTKISMainPlayerConfig.Builder` ; VideoFeed lit automatiquement les SharedPreferences IAB.

**Cookies debug** (`CookieManagerScreen`) : deux champs (nom/valeur cookie), bouton "Ajouter" → `CookieManager.getInstance().setCookie(".ultimedia.com", "$name=$value; Domain=.ultimedia.com; Path=/")` + `flush()`. Suppression individuelle via cookie expiré (`Max-Age=0`), "Tout supprimer" via `removeAllCookies(null)`. Les cookies persistent entre sessions (CookieManager natif Android).

## Règles absolues

1. Ne jamais committer `local.properties` (contient le token JitPack).
2. Ordre d'initialisation strict : `Application.onCreate()` → UMP puis InStream SDK ; `MainActivity.onCreate()` → consentement UMP puis affichage UI. La TC String doit être disponible AVANT de configurer les players Digiteka.
3. `AndroidView` en Compose pour `MainPlayerView` / `VideoFeedCarrousel` (Views Android classiques) — attention au lifecycle.
4. Permission manifest obligatoire : `<uses-permission android:name="android.permission.INTERNET"/>`.
5. Déclarer `android:name=".DigitekaTestApp"` dans `<application>` du manifest.
6. Appeler `CookieManager.getInstance().setAcceptCookie(true)` dans `Application.onCreate()`.
7. ProGuard (si build release activé) : ajouter les règles nécessaires pour les SDK Digiteka (consulter leur doc).
8. Privilégier un `libs.versions.toml` pour centraliser les versions de dépendances.

## Style

Material 3, thème clair par défaut. Boutons HomeScreen : `ElevatedCard` ou `FilledTonalButton` grande taille. Accent bleu Digiteka `#1A73E8` (ou couleurs Material par défaut).

## Validation

```bash
./gradlew assembleDebug   # build debug
./gradlew installDebug    # installer sur device/émulateur connecté
./gradlew lint            # vérifications lint
```
