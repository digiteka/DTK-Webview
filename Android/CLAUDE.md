# CLAUDE.md — Digiteka SDK Test App

## Objectif du projet

Application Android de test pour valider l'intégration de deux SDK Digiteka :
- **SDK VideoFeed** : carousel vidéo + lecteur vertical (style TikTok)
- **SDK InStream** : lecteur vidéo flottant dans un contexte d'article

L'app doit également intégrer une CMP (Consent Management Platform) pour tester la monétisation dans un cadre RGPD.

---

## Stack technique

- **Langage** : Kotlin
- **UI** : Jetpack Compose (avec interop AndroidView pour les composants SDK en XML)
- **Min SDK** : 24
- **Target SDK** : 35
- **Build** : Gradle Kotlin DSL (`.gradle.kts`)
- **Architecture** : Navigation Compose (NavHost) avec 4 destinations

---

## Dépendances

### SDK Digiteka (via JitPack privé)

Dans `settings.gradle.kts`, configurer le repository JitPack avec authentification :

```kotlin
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.Properties

val localProperties = Properties().apply {
    val localProperties = file("local.properties")
    if (localProperties.exists() && localProperties.isFile) {
        InputStreamReader(FileInputStream(localProperties), Charsets.UTF_8).use { reader ->
            load(reader)
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
            credentials {
                username = localProperties.getProperty("DIGITEKA_JITPACK_ACCESS_KEY")
            }
        }
    }
}
```

Dans `build.gradle.kts` (module app) :

```kotlin
dependencies {
    // Digiteka VideoFeed
    implementation("com.github.digiteka:videofeed-android:2.1.1")

    // Digiteka InStream
    implementation("com.github.digiteka:SDK-instream-Android:1.1.0-0")

    // Google UMP (CMP gratuite)
    implementation("com.google.android.ump:user-messaging-platform:3.1.0")
}
```

### Fichier `local.properties` (NE PAS COMMITTER)

Claude Code doit créer/compléter `local.properties` à la racine du projet avec le token JitPack :

```properties
DIGITEKA_JITPACK_ACCESS_KEY=jp_fuceltdfpdjulq01r2ca4js464
```

Ce token est utilisé pour les deux SDK (VideoFeed et InStream) via le même repository JitPack.

---

## Configuration CMP — Google UMP

### Prérequis

L'utilisateur doit avoir un compte AdMob (gratuit) sur https://apps.admob.com et :
1. Enregistrer l'app dans AdMob pour obtenir un `APPLICATION_ID` (format `ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY`)
2. Dans AdMob > Confidentialité et messages > Réglementation européenne, créer et publier un message RGPD
3. Ajouter dans `AndroidManifest.xml` :

```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY"/>
```

### Intégration dans le code

Initialiser et afficher le consentement au lancement de l'app (dans `MainActivity` ou la classe `Application`).

Flux UMP :
1. `requestConsentInfoUpdate()` — vérifie si le consentement est nécessaire
2. `loadAndShowConsentFormIfRequired()` — affiche le formulaire si besoin
3. Récupérer la TC String depuis `SharedPreferences` (clé `IABTCF_TCString`) pour la passer aux SDK Digiteka

Pour le **debug en zone non-EEE**, forcer la géographie :

```kotlin
val debugSettings = ConsentDebugSettings.Builder(this)
    .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
    .addTestDeviceHashedId("YOUR_TEST_DEVICE_HASHED_ID") // visible dans logcat
    .build()

val params = ConsentRequestParameters.Builder()
    .setConsentDebugSettings(debugSettings)
    .build()
```

### Passage de la consent string aux SDK Digiteka

Après consentement, lire la TC String :

```kotlin
val prefs = PreferenceManager.getDefaultSharedPreferences(context)
val tcString = prefs.getString("IABTCF_TCString", null)
```

- Pour **InStream** : passer dans `DTKISMainPlayerConfig.Builder(..., gdprConsentString = tcString, ...)`
- Pour **VideoFeed** : le SDK lit automatiquement les SharedPreferences IAB (vérifier dans la doc Digiteka)

---

## Paramètres SDK Digiteka

### SDK InStream

| Paramètre | Valeur |
|-----------|--------|
| MDTK | `01850262` |
| zone | `1` |
| src (video ID) | `xrkxv0k` |
| urlReferrer | `https://test-app.digiteka.com/article` |
| playMode | `PlayMode.VISIBLE_AT_FIFTY_PERCENT` |

### SDK VideoFeed

| Paramètre | Valeur |
|-----------|--------|
| MDTK | `01573101` |
| zoneId | `null` (pas spécifié) |
| adUnitPath | `null` (pas spécifié) |

---

## Architecture de l'application

### Écrans

L'app comporte une **HomeScreen** avec 4 gros boutons (style Material 3, pleine largeur, hauteur ~120dp, avec icône et texte) :

#### 1. Bouton "InStream Player" → `InstreamArticleScreen`
- Simule un article de presse avec du texte Lorem Ipsum
- Contenu scrollable (LazyColumn ou Column avec verticalScroll)
- Au milieu de l'article : un `MainPlayerView` (composant InStream) intégré via `AndroidView` en Compose
- Ratio 16:9
- Le player flottant (VisiblePlayer) doit apparaître quand le MainPlayer sort du viewport
  - Position : `BOTTOM_END`
  - `widthPercent = 0.5f`
  - ratio "16:9"
  - margins 12dp

**Intégration InStream (dans Application.onCreate)** :

```kotlin
val dtkisConfig = DTKISConfig.Builder(mdtk = "01850262").build()
InStream.initialize(applicationContext = applicationContext, config = dtkisConfig)
```

**Intégration InStream (dans l'écran article)** :

```kotlin
val mainPlayerConfig = DTKISMainPlayerConfig.Builder(
    zone = "1",
    gdprConsentString = tcString, // récupéré via UMP
    src = "xrkxv0k",
    urlReferrer = "https://test-app.digiteka.com/article",
    tagParam = null
).setPlayMode(PlayMode.VISIBLE_AT_FIFTY_PERCENT).build()

// Dans un AndroidView Compose :
val mainPlayerView = remember { MainPlayerView(context) }
val mainPlayerKey = remember {
    InStream.configureMainPlayer(mainPlayerView, mainPlayerConfig)
}

// Puis attacher le visible player :
InStream.attachVisiblePlayerTo(
    parent = rootView, // le root FrameLayout/Box
    visiblePlayerConfig = DTKISVisiblePlayerConfig(
        widthPercent = 0.5f,
        position = Position.BOTTOM_END,
        ratio = "16:9",
        horizontalMargin = 12f,
        verticalMargin = 12f
    ),
    mainPlayerKey = mainPlayerKey
)
```

#### 2. Bouton "VideoFeed Carousel" → `CarouselScreen`
- Affiche un `VideoFeedCarousel` via `AndroidView` en Compose
- Hauteur du carousel : ~280dp
- Chargé via `carousel.load(mdtk = "01573101", adUnitPath = null, zoneId = null)`
- En dessous du carousel : du texte d'explication ou du Lorem Ipsum pour montrer l'intégration dans un contexte de page

#### 3. Bouton "VideoFeed Plein Écran" → Lance directement l'Activity VideoFeed
- Ne navigue PAS vers un nouvel écran Compose
- Lance directement :

```kotlin
context.startActivity(
    VideoFeedActivity.newInstance(
        context = context,
        mdtk = "01573101",
        videoId = null,       // ouvre sur la première vidéo du feed
        adUnitPath = null,
        zoneId = null
    )
)
```

### Navigation

```
NavHost(startDestination = "home") {
    composable("home") { HomeScreen(navController) }
    composable("instream") { InstreamArticleScreen() }
    composable("carousel") { CarouselScreen() }
    composable("cookies") { CookieManagerScreen() }
    // Pas de destination pour VideoFeed plein écran (Activity externe)
}
```

#### 4. Bouton "Debug Cookies" → `CookieManagerScreen`

Écran de gestion dynamique des cookies sur le domaine `.ultimedia.com`, pour le debug des players Digiteka.

**Interface :**
- En haut : deux champs `OutlinedTextField` côte à côte (ou empilés) :
  - "Nom du cookie" (placeholder : ex. `debug`)
  - "Valeur du cookie" (placeholder : ex. `true`)
- Un bouton "Ajouter" qui injecte le cookie via `android.webkit.CookieManager`
- En dessous : la liste des cookies actuellement posés sur `.ultimedia.com`, chaque ligne affichant `nom=valeur` avec un bouton "Supprimer" (icône poubelle)
- Un bouton "Tout supprimer" en bas

**Implémentation technique :**

```kotlin
import android.webkit.CookieManager

// Ajouter un cookie
fun setCookie(name: String, value: String) {
    val cookieManager = CookieManager.getInstance()
    cookieManager.setAcceptCookie(true)
    cookieManager.setCookie(".ultimedia.com", "$name=$value; Domain=.ultimedia.com; Path=/")
    cookieManager.flush()
}

// Lire tous les cookies du domaine
fun getCookies(): List<Pair<String, String>> {
    val cookieManager = CookieManager.getInstance()
    val cookieString = cookieManager.getCookie(".ultimedia.com") ?: return emptyList()
    return cookieString.split(";")
        .map { it.trim() }
        .filter { it.contains("=") }
        .map {
            val parts = it.split("=", limit = 2)
            parts[0].trim() to (parts.getOrNull(1)?.trim() ?: "")
        }
}

// Supprimer un cookie spécifique (en le rendant expiré)
fun removeCookie(name: String) {
    val cookieManager = CookieManager.getInstance()
    cookieManager.setCookie(
        ".ultimedia.com",
        "$name=; Domain=.ultimedia.com; Path=/; Max-Age=0"
    )
    cookieManager.flush()
}

// Tout supprimer
fun removeAllCookies() {
    val cookieManager = CookieManager.getInstance()
    cookieManager.removeAllCookies(null)
    cookieManager.flush()
}
```

**Comportement :**
- Les cookies persistent entre les sessions (gérés par le `CookieManager` Android natif)
- Après ajout/suppression, la liste se rafraîchit immédiatement
- Les cookies sont partagés avec les WebViews internes des SDK Digiteka (InStream et VideoFeed) puisqu'ils utilisent le même `CookieManager` système
- Ajouter un `Snackbar` de confirmation après chaque action

---

## Structure des fichiers

```
app/src/main/java/com/example/digiteka_test/
├── DigitekaTestApp.kt          // Application class (init InStream + UMP)
├── MainActivity.kt             // Compose Activity + NavHost
├── ui/
│   ├── HomeScreen.kt           // 4 gros boutons
│   ├── InstreamArticleScreen.kt // Faux article + InStream player
│   ├── CarouselScreen.kt       // VideoFeed Carousel
│   └── CookieManagerScreen.kt  // Gestion cookies debug ultimedia.com
└── consent/
    └── ConsentManager.kt       // Helper pour Google UMP (init, récupérer TC string)
```

---

## Points d'attention

1. **AndroidView en Compose** : les composants `MainPlayerView` et `VideoFeedCarousel` sont des View Android classiques. Utiliser `AndroidView` en Compose pour les intégrer. Attention au lifecycle.

2. **Permissions** : ajouter dans `AndroidManifest.xml` :
   ```xml
   <uses-permission android:name="android.permission.INTERNET"/>
   ```

3. **Application class** : déclarer dans le manifest :
   ```xml
   <application
       android:name=".DigitekaTestApp"
       ...>
   ```

4. **ProGuard** : si le build release est activé, ajouter les règles ProGuard nécessaires pour les SDK Digiteka (consulter leur documentation).

5. **Test du consentement UMP** : en développement, utiliser `setDebugGeography(DEBUG_GEOGRAPHY_EEA)` et ajouter le device hash ID pour forcer l'affichage du formulaire RGPD même hors EEE.

6. **Ordre d'initialisation** :
   - `Application.onCreate()` : initialiser Google UMP, puis InStream SDK
   - `MainActivity.onCreate()` : demander le consentement UMP, puis afficher l'UI
   - La TC String doit être disponible AVANT de configurer les players Digiteka

7. **Cookies debug** : le `android.webkit.CookieManager` est partagé par toutes les WebViews du process. Les cookies posés via `CookieManagerScreen` seront automatiquement envoyés par les WebViews internes des SDK Digiteka. Penser à appeler `CookieManager.getInstance().setAcceptCookie(true)` dans `Application.onCreate()`.

8. **Version catalog** : privilégier un `libs.versions.toml` pour centraliser les versions des dépendances.

---

## Style visuel

- Material 3 (Material Design 3)
- Thème clair par défaut
- Boutons de la HomeScreen : `ElevatedCard` ou `FilledTonalButton` grande taille
- Couleurs Digiteka pour le branding : bleu `#1A73E8` comme accent (ou garder les couleurs Material par défaut)

---

## Commandes utiles

```bash
# Build debug
./gradlew assembleDebug

# Installer sur device/émulateur connecté
./gradlew installDebug

# Lancer les vérifications lint
./gradlew lint
```
