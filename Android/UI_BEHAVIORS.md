# UI_BEHAVIORS.md — Digiteka Test App

> Reference document for iOS port. Describes every screen's layout, states, user interactions, validations, error messages, and navigation flows as implemented in the Android app.

---

## Table of Contents

1. [App-Level Behaviors](#1-app-level-behaviors)
2. [HomeScreen](#2-homescreen)
3. [InstreamArticleScreen](#3-instreamarticlescreen)
4. [NoSdkPlayerScreen](#4-nosdk-player-screen)
5. [CarrouselScreen](#5-carrouselscreen)
6. [CookieManagerScreen](#6-cookiemanagerscreen)
7. [InstreamConfigScreen](#7-instreamconfigscreen)
8. [VideoFeedConfigScreen](#8-videofeedconfigscreen)
9. [VideoFeedActivity (external)](#9-videofeedactivity-external)
10. [Visual Design System](#10-visual-design-system)

---

## 1. App-Level Behaviors

### 1.1 Consent Gate (Google UMP / RGPD)

On every cold launch, before the UI renders:

1. The app requests consent info update from Google UMP.
2. If the user is in the EEA (or debug EEA mode is active), a RGPD consent form is presented modally, blocking all interaction.
3. The main UI (NavHost / navigation) is **not rendered** until the consent callback fires — regardless of whether the user accepted, declined, or if an error occurred.
4. If the form fails to load or an error occurs, the app proceeds silently (non-blocking — logs a warning in console, shows no UI error to the user).
5. After consent is gathered, the HomeScreen becomes visible.

**State**: `consentReady: Boolean` — starts `false`, set to `true` in the consent callback.

**What the user sees during consent loading**: A blank screen (system window background color: `#000000`).

### 1.2 Global Initialization (on app start, before any screen)

- `android.webkit.CookieManager.setAcceptCookie(true)` is called globally.
- InStream SDK is initialized with MDTK read from SharedPreferences (default: `01850262`).
  - If initialization fails, the error is logged silently; the app continues normally.
- WebView remote debugging is enabled (developer feature, no UI impact).
- A lifecycle callback is registered to automatically enable third-party cookies on any WebView created within any activity.

---

## 2. HomeScreen

### 2.1 Layout

- **Background**: solid black (`#000000`), full screen.
- **Top**: Digiteka logo image, centered horizontally, fixed height (approx. 56dp), with padding above and below.
- **Body**: vertically scrollable list of 7 cards, each full-width.
- **No top app bar, no bottom navigation bar.**

### 2.2 Cards (in order)

Each card is a clickable row with:
- Left: colored icon box with radial gradient background (blue tones), containing a white vector icon.
- Center: two lines of text — bold title (white, `#FFFFFF`) and subtitle (grey, `#9E9E9E`).
- Right: chevron arrow icon (grey).
- Border: thin `#2C2C2C` stroke.
- Background: `#181818`.
- Corner radius: rounded (approx. 12dp).
- Card height: auto, content-driven (approx. 80–100dp).
- Spacing between cards: approx. 12dp.

| # | Title | Subtitle | Navigation target |
|---|-------|----------|------------------|
| 1 | SDK Instream | Intégration via le SDK natif | InstreamArticleScreen |
| 2 | Instream sans SDK | Lecture via WebView / iframe | NoSdkPlayerScreen |
| 3 | VideoFeed Carrousel | Intégration du carrousel vidéo | CarrouselScreen |
| 4 | VideoFeed Plein Écran | Lance l'Activity VideoFeed | VideoFeedActivity (external) |
| 5 | Debug Cookies | Gérer les cookies ultimedia.com | CookieManagerScreen |
| 6 | Config InStream | Paramètres SDK & WebView | InstreamConfigScreen |
| 7 | Config VideoFeed | Paramètres MDTK, zone, ad unit | VideoFeedConfigScreen |

### 2.3 Interactions

- **Tapping any card**: navigates immediately to the corresponding destination (no confirmation, no loading state).
- **Card 4 (VideoFeed Plein Écran)**: does NOT navigate within the app's nav graph. Instead, launches `VideoFeedActivity` as a new Android Activity (equivalent of a full-screen modal presented from a separate view controller on iOS). Uses the MDTK/zoneId/adUnitPath/videoId from saved VideoFeed config (defaults if not configured).
- **Scrolling**: the list scrolls vertically if the screen is too short to show all 7 cards (rare but possible on small screens).

### 2.4 States

| State | Description |
|-------|-------------|
| Normal | All 7 cards visible, no highlighted state |
| Pressed | Card background slightly darkens (Material ripple effect) — transient |

There are no loading, error, or empty states on the HomeScreen.

---

## 3. InstreamArticleScreen

### 3.1 Purpose

Simulates a news article page with an embedded InStream video player positioned mid-article, and a floating "visible player" that appears when the main player scrolls out of view.

### 3.2 Layout

- **Background**: black (`#000000`).
- **Top app bar**: back arrow (←) on the left, title "Article InStream" centered, white text on dark background.
- **Body**: vertically scrollable column.
  - **Text block 1**: 6–7 paragraphs of Lorem Ipsum article text (white, body font size, readable line height).
  - **Video player slot**: `MainPlayerView` rendered at 16:9 aspect ratio (full column width × 9/16 height). While the SDK loads, this area shows a black rectangle.
  - **Text block 2**: 6–7 more Lorem Ipsum paragraphs below the player.
- **Floating visible player**: appears bottom-right of screen, 65% of screen width, 16:9 ratio, 12dp margins from screen edges + navigation bar height. Appears automatically when the main player scrolls out of the viewport (≥50% hidden). Disappears when the main player returns to view.

### 3.3 Configuration Read on Entry

When this screen opens, it reads from SharedPreferences:
- `mdtk` (default: `01850262`)
- `zone` (default: `"1"`)
- `src` / video ID (default: `383kuzf`)
- `urlReferrer` (default: `https://test-app.digiteka.com/article`)
- `playMode` (default: `ON_CLICK`)
- TC String from IAB SharedPreferences key `IABTCF_TCString` (may be null if no consent was given or outside EEA).

### 3.4 Player Initialization Flow

1. `MainPlayerView` is created (a native Android View wrapping a WebView).
2. `InStream.configureMainPlayer(mainPlayerView, config)` is called → returns a `mainPlayerKey`.
3. After the composable is fully laid out (root view is available), `InStream.attachVisiblePlayerTo(rootView, visiblePlayerConfig, mainPlayerKey)` is called.
4. Third-party cookies are enabled on any WebView found inside `MainPlayerView` (via recursive traversal).

### 3.5 Play Modes

| Mode | Behavior |
|------|----------|
| `ON_CLICK` | Video starts only when user taps the player |
| `VISIBLE_AT_FIFTY_PERCENT` | Video autoplays when ≥50% of the player is visible |
| `AUTOPLAY` | Video autoplays immediately |

### 3.6 Visible Player Behavior

- Trigger: main player scrolls off screen (less than 50% visible).
- Appearance: animated slide-in from bottom-right corner.
- Position: bottom-right, with 12dp margins + safe area / navigation bar inset.
- Width: 65% of screen width.
- Aspect ratio: 16:9 (height auto-calculated).
- The visible player continues playback from where the main player left off.
- When user scrolls back to the main player: visible player disappears.
- The visible player has a close (×) button (provided by SDK, not custom).

### 3.7 Lifecycle / Cleanup

- On screen exit (back navigation):
  - `InStream.detachVisiblePlayer(mainPlayerKey)` equivalent is called (child views removed from root).
  - All WebViews inside `MainPlayerView` are destroyed (`stopLoading`, `loadUrl("about:blank")`, `destroy()`).

### 3.8 States

| State | Description |
|-------|-------------|
| Loading | Black rectangle at player slot, article text already visible |
| Playing | Video plays in player slot |
| Floating | Visible player shown bottom-right, main player off-screen |
| Error (SDK) | If SDK initialization failed at app start, the player slot remains black (no error message shown to user) |

### 3.9 Navigation

- Back arrow (top-left) or system back gesture → returns to HomeScreen.
- No other navigation from this screen.

---

## 4. NoSdk Player Screen

### 4.1 Purpose

Loads an InStream player via a WebView iframe (without the native SDK). Used to test the player integration in a pure web context.

### 4.2 Layout

Identical structure to InstreamArticleScreen:
- Top app bar: back arrow, title "Article sans SDK".
- Vertically scrollable column with Lorem Ipsum text, player in the middle, text below.
- Player rendered as a WebView at 16:9 ratio.

### 4.3 Configuration Read on Entry

Reads from "instream_nosdk_prefs" SharedPreferences:
- `mdtk` (default: `01237780`)
- `zone` (default: `25`)
- `src` / video ID (default: `3lflr3r`)
- `urlReferrer` (default: `https://test-app.digiteka.com/article`)
- `host` (default: `www.ultimedia.com`)
- TC String from `IABTCF_TCString`.

### 4.4 Iframe URL Format

The WebView loads an HTML page containing an iframe with this URL structure:

```
https://{host}/deliver/generic/iframe/mdtk/{mdtk}/zone/{zone}/src/{src}/...
```

Additional query parameters appended:
- `gdpr_consent={tcString}` (if TC String is present)
- `canonicalUrl={urlReferrer}` (URL-encoded)

The HTML wrapper uses the `padding-bottom: 56.25%` CSS trick to maintain 16:9 aspect ratio responsively.

### 4.5 WebView Settings

- JavaScript: enabled.
- DOM storage: enabled.
- Media autoplay without user gesture: enabled.
- Cache mode: `LOAD_NO_CACHE`.
- Third-party cookies: enabled.
- Custom `WebChromeClient` for fullscreen support.

### 4.6 Fullscreen Video Support

When the user taps the fullscreen button inside the player:
1. The app hides the system UI (navigation bar, status bar) — immersive fullscreen.
2. The video view is placed in a dedicated `FrameLayout` overlaid on the screen.
3. When the user exits fullscreen (back button or player UI), the overlay is removed and system UI is restored.

### 4.7 States

| State | Description |
|-------|-------------|
| Loading | White/blank WebView, article text visible |
| Playing | Video playing in iframe |
| Fullscreen | Video occupies entire screen, system UI hidden |
| Error | If URL is malformed or network unavailable, WebView shows its native error page (no custom error UI) |

### 4.8 Lifecycle / Cleanup

- On screen exit: WebView is destroyed (same pattern as InstreamArticleScreen).
- FrameLayout overlay is removed from decorView.

---

## 5. CarrouselScreen

### 5.1 Purpose

Displays a VideoFeed horizontal video carrousel, simulating an editorial page integration.

### 5.2 Layout

- Top app bar: back arrow, title "VideoFeed Carrousel".
- Vertically scrollable column:
  - **Title text** above carrousel: "Vidéos recommandées" (or similar heading).
  - **Carrousel**: `VideoFeedCarrousel` rendered via AndroidView, full screen width, **height: 280dp**. The carrousel scrolls horizontally within this fixed height.
  - **Description text** below carrousel: Lorem Ipsum paragraphs explaining the integration context.

### 5.3 Configuration Read on Entry

Reads from "videofeed_prefs" SharedPreferences:
- `mdtk` (default: `01573101`)
- `zoneId` (default: null — not specified)
- `adUnitPath` (default: null — not specified)

Carrousel is loaded via: `carrousel.load(mdtk, adUnitPath, zoneId)`.

### 5.4 Carrousel Interactions

- **Horizontal swipe**: browse video thumbnails.
- **Tap on a video**: the VideoFeed SDK handles this internally (typically opens a fullscreen vertical video feed — TikTok-style). This is managed entirely by the SDK.

### 5.5 States

| State | Description |
|-------|-------------|
| Loading | 280dp black rectangle, no spinner (SDK handles internally) |
| Loaded | Horizontal thumbnail strip visible |
| Error | If SDK/network fails, the 280dp area remains blank (no custom error UI) |

### 5.6 Lifecycle / Cleanup

- On screen exit: all WebViews within the carrousel are destroyed.
- Third-party cookies are enabled on WebViews found inside the carrousel widget on layout.

### 5.7 Navigation

- Back arrow → HomeScreen.
- Tapping a video card → VideoFeed full-screen player (SDK-managed, modal-style).

---

## 6. CookieManagerScreen

### 6.1 Purpose

Developer debug tool to read, add, and remove cookies on the `.ultimedia.com` domain. Cookies set here are shared with all WebViews in the app (InStream, VideoFeed) because all WebViews use the same system CookieManager.

### 6.2 Layout

- Top app bar: back arrow, title "Debug Cookies".
- Scrollable column:
  1. **Input section** (top): two fields in a horizontal row.
     - `OutlinedTextField` — "Nom du cookie" (cookie name), placeholder: `ex. debug`
     - `OutlinedTextField` — "Valeur" (cookie value), placeholder: `ex. true`
     - Below fields: "Ajouter" button (full-width or right-aligned).
  2. **Cookie list** (middle): scrollable list of currently set cookies on `.ultimedia.com`.
     - Each row: `nom=valeur` text on the left, trash icon button on the right.
     - If no cookies are set: an empty state message ("Aucun cookie") or simply an empty list.
  3. **Remove all button** (bottom): "Tout supprimer" — full-width destructive button.
- **Snackbar**: appears at bottom of screen for action confirmations.

### 6.3 State Variables

| Variable | Type | Description |
|----------|------|-------------|
| `cookieName` | String | Controlled value of the name text field |
| `cookieValue` | String | Controlled value of the value text field |
| `cookies` | List of (name, value) pairs | Currently active cookies, refreshed after each action |

### 6.4 Cookie List Refresh

The cookie list is read from `CookieManager.getCookie(".ultimedia.com")` and parsed. It refreshes:
- On screen entry.
- After every add, remove, or remove-all action.

### 6.5 User Interactions

#### Add Cookie
1. User types a name in the "Nom du cookie" field.
2. User types a value in the "Valeur" field.
3. User taps "Ajouter".
4. **Validation**: if either field is blank, the cookie is still added (no client-side validation enforced — empty strings are passed through).
5. `CookieManager.setCookie(".ultimedia.com", "name=value; Domain=.ultimedia.com; Path=/; SameSite=None; Secure")` is called.
6. `CookieManager.flush()` is called to persist.
7. Cookie list refreshes.
8. Snackbar: **"Cookie ajouté"** (approx. 2 seconds, bottom of screen).
9. The text fields are **not cleared** after adding (user can keep editing).

#### Remove One Cookie
1. User taps the trash icon on a cookie row.
2. The cookie is expired immediately: `setCookie(".ultimedia.com", "name=; Domain=.ultimedia.com; Path=/; Max-Age=0")`.
3. `CookieManager.flush()` is called.
4. Cookie list refreshes.
5. Snackbar: **"Cookie supprimé"**.

#### Remove All Cookies
1. User taps "Tout supprimer".
2. `CookieManager.removeAllCookies(null)` is called (system-level, removes ALL cookies across ALL domains).
3. `CookieManager.flush()` is called.
4. Cookie list refreshes (becomes empty).
5. Snackbar: **"Tous les cookies supprimés"**.

> ⚠️ **iOS note**: "Remove all" removes cookies system-wide (not just `.ultimedia.com`). This is a limitation of the Android API. On iOS, implement domain-scoped removal if possible.

### 6.6 Snackbar Behavior

- Appears at the bottom of the screen above the navigation bar.
- Duration: short (~2 seconds).
- No action button.
- Only one snackbar visible at a time (new one replaces old if actions are rapid).

### 6.7 States

| State | Description |
|-------|-------------|
| Empty | Cookie list shows no rows (or "Aucun cookie" label) |
| Populated | List shows one row per `name=value` pair |
| Action pending | Snackbar visible |

---

## 7. InstreamConfigScreen

### 7.1 Purpose

Allows the developer to override the InStream SDK and no-SDK player parameters without recompiling the app. Settings are persisted in SharedPreferences and take effect on next navigation to the player screens.

### 7.2 Layout

- Top app bar: back arrow, title "Config InStream".
- Scrollable column divided into **two independent sections**:
  1. **SDK InStream** configuration.
  2. **InStream sans SDK** (WebView/iframe) configuration.
- Each section has a title, a horizontal divider, form fields, save/reset buttons, and a read-only display of the currently active (saved) config.

### 7.3 Section 1 — SDK InStream Config

#### Fields

| Field | Label | Placeholder / Hint | Default value |
|-------|-------|--------------------|---------------|
| MDTK | "MDTK" | `01850262` | `01850262` |
| Zone | "Zone" | `1` | `1` |
| Src (Video ID) | "Src (Video ID)" | `383kuzf` | `383kuzf` |
| URL Référent | "URL Référent" | `https://test-app.digiteka.com/article` | same |
| Play Mode | "Mode de lecture" | (segmented selector) | `ON_CLICK` |

#### Play Mode Selector

A `SingleChoiceSegmentedButtonRow` (segmented control) with 3 options:
- **"Au clic"** → `ON_CLICK`
- **"50% visible"** → `VISIBLE_AT_FIFTY_PERCENT`
- **"Auto"** → `AUTOPLAY`

Exactly one option is always selected.

#### MDTK Warning

Below the MDTK field, a small warning text:
> "⚠ Les changements du MDTK prennent effet au prochain démarrage de l'app."

This is informational only (no visual emphasis beyond the warning icon character).

#### Buttons

- **"Sauvegarder"**: saves all 5 fields to "instream_sdk_prefs" SharedPreferences. Empty strings are saved as-is (treated as "use default" at read time). Shows snackbar: **"Configuration SDK sauvegardée"**.
- **"Réinitialiser"**: removes all keys from "instream_sdk_prefs", resets form fields to default values, refreshes the "active config" display. Shows snackbar: **"Configuration SDK réinitialisée"**.

#### Active Config Display (read-only)

A grey box below the buttons showing the currently persisted config values:
```
MDTK: 01850262
Zone: 1
Src: 383kuzf
URL: https://test-app.digiteka.com/article
Mode: ON_CLICK
```
Updates immediately after save or reset.

### 7.4 Section 2 — InStream sans SDK Config

#### Fields

| Field | Label | Default value |
|-------|-------|---------------|
| MDTK | "MDTK" | `01237780` |
| Zone | "Zone" | `25` |
| Src (Video ID) | "Src (Video ID)" | `3lflr3r` |
| URL Référent | "URL Référent" | `https://test-app.digiteka.com/article` |
| Host | "Host" | `www.ultimedia.com` |

#### Buttons

- **"Sauvegarder"**: saves to "instream_nosdk_prefs". Snackbar: **"Configuration sans SDK sauvegardée"**.
- **"Réinitialiser"**: resets to defaults. Snackbar: **"Configuration sans SDK réinitialisée"**.

#### Active Config Display

Same pattern as Section 1. Shows 5 values including Host.

### 7.5 Form Field Behavior

- Fields use `rememberSaveable` — values survive screen rotation.
- No real-time validation. Any string is accepted.
- Fields support standard text editing (cut, copy, paste, cursor positioning).
- Keyboard type: default (text, not numeric) for all fields.
- The active config display reads directly from SharedPreferences (not from the form fields), so it always shows what is truly saved.

### 7.6 Navigation

- Back arrow → HomeScreen.
- No unsaved-changes warning on back navigation.

---

## 8. VideoFeedConfigScreen

### 8.1 Purpose

Allows the developer to override VideoFeed SDK parameters. Persisted in SharedPreferences, applied on next launch of the Carrousel or VideoFeed full-screen.

### 8.2 Layout

- Top app bar: back arrow, title "Config VideoFeed".
- Scrollable column with form fields, save/reset buttons, and active config display.

### 8.3 Fields

| Field | Label | Hint / Placeholder | Notes |
|-------|-------|--------------------|-------|
| MDTK | "MDTK" | `01573101` | Required for SDK |
| Zone ID | "Zone ID" | `(optionnel)` | Optional; null if empty |
| Ad Unit Path | "Ad Unit Path" | `/{networkCode}/{adBlockPath}` | Optional; null if empty |
| Video ID | "Video ID" | `(optionnel, plein écran uniquement)` | Only used for VideoFeedActivity launch |

### 8.4 Buttons

- **"Sauvegarder"**: saves all 4 fields to "videofeed_prefs" SharedPreferences. Empty string input → saved as empty string, read back as null at usage point. Snackbar: **"Configuration VideoFeed sauvegardée"**.
- **"Réinitialiser"**: removes all keys, resets form fields to empty (except MDTK which resets to `01573101`). Snackbar: **"Configuration VideoFeed réinitialisée"**.

### 8.5 Active Config Display

Read-only box showing current saved values. If a value is null/empty, displays with "(défaut)" suffix:
```
MDTK: 01573101
Zone ID: (défaut)
Ad Unit Path: (défaut)
Video ID: (défaut)
```

### 8.6 Form Field Behavior

- Same as InstreamConfigScreen: `rememberSaveable`, no validation, any string accepted.
- No unsaved-changes warning on back navigation.

### 8.7 Navigation

- Back arrow → HomeScreen.

---

## 9. VideoFeedActivity (external)

### 9.1 Behavior

- Launched from HomeScreen card 4 via `startActivity()`.
- This is the VideoFeed SDK's own built-in Activity — the app does not control its UI.
- Displays a full-screen vertical video feed (TikTok-style scroll).
- The SDK reads `mdtk`, `videoId`, `adUnitPath`, `zoneId` from the intent extras passed at launch.
- User exits by pressing the system back button or using the SDK's own close control.
- On exit, the user returns to HomeScreen (the activity stack pops back).

### 9.2 Parameters Passed

From "videofeed_prefs" SharedPreferences (defaults used if not configured):
- `mdtk` = `01573101`
- `videoId` = null (opens on first video of the feed)
- `adUnitPath` = null
- `zoneId` = null

---

## 10. Visual Design System

### 10.1 Color Palette

| Name | Hex | Usage |
|------|-----|-------|
| `DigiBlue` | `#1A73E8` | Primary actions, icon backgrounds, active state |
| `DigiBlueLight` | `#5B9BD5` | Secondary, icon gradient |
| `DigiBlack` | `#000000` | App background |
| `DigiSurface` | `#0F0F0F` | Surface (elevated elements) |
| `DigiCard` | `#181818` | Card / list item background |
| `DigiCardBorder` | `#2C2C2C` | Card border stroke |
| `DigiTextPrimary` | `#FFFFFF` | Primary text |
| `DigiTextSecondary` | `#9E9E9E` | Subtitle / hint text |
| `DigiError` | `#CF6679` | Error text (used in theme, not currently triggered in UI) |

### 10.2 Typography

- System default font (Material 3 defaults).
- Body text: readable size (~14–16sp equivalent).
- Section titles: bold, ~18sp.
- Subtitles / hints: `DigiTextSecondary`, ~12–13sp.
- Top app bar titles: bold, ~18–20sp, white.

### 10.3 Spacing & Sizing

| Element | Size |
|---------|------|
| HomeScreen card height | ~80–100dp (content-driven) |
| HomeScreen card corner radius | ~12dp |
| HomeScreen card horizontal margin | ~16dp |
| HomeScreen card-to-card gap | ~12dp |
| Carrousel height | 280dp |
| Main player aspect ratio | 16:9 (full width) |
| Visible player width | 65% of screen width |
| Visible player margins | 12dp (horizontal) + 12dp + nav bar height (vertical) |
| OutlinedTextField border | 1dp, `DigiBlue` when focused, `DigiCardBorder` when idle |
| Top app bar height | Standard Material 3 (~56dp) |

### 10.4 Theme

- **Mode**: Dark only (no light mode variant).
- **Material**: Material Design 3.
- All screens inherit the global `DIGITESTTheme`.

### 10.5 Navigation Chrome

- **Top app bar**: present on all screens except HomeScreen. Title centered (or start-aligned with back arrow). No overflow menu. Back button = system back gesture equivalent.
- **Status bar**: transparent / edge-to-edge (Android 15 edge-to-edge mode). Content drawn behind system bars with appropriate inset padding.
- **Navigation bar**: transparent / edge-to-edge. Screens account for navigation bar height where needed (e.g., visible player bottom margin).

---

## 11. Navigation Map

```
ConsentForm (modal, blocking) ──► HomeScreen
                                      │
              ┌───────────────────────┼────────────────────────┐
              │                       │                        │
   InstreamArticleScreen    NoSdkPlayerScreen          CarrouselScreen
              │                       │                        │
              └───────────────────────┘────────────────────────┘
                                      │
              ┌───────────────────────┼────────────────────────┐
              │                       │                        │
   CookieManagerScreen   InstreamConfigScreen    VideoFeedConfigScreen
              │                       │                        │
              └───────────────────────┘────────────────────────┘
                                      │
                              VideoFeedActivity
                           (external, system back to return)
```

**All back navigation** (from any screen back to HomeScreen) is handled by the top app bar back arrow or the system back gesture. No transitions are customized (default Material 3 slide animation).

---

## 12. SharedPreferences Keys Reference

### "instream_sdk_prefs"

| Key | Type | Default |
|-----|------|---------|
| `mdtk` | String | `01850262` |
| `zone` | String | `1` |
| `src` | String | `383kuzf` |
| `urlReferrer` | String | `https://test-app.digiteka.com/article` |
| `playMode` | String | `ON_CLICK` |

### "instream_nosdk_prefs"

| Key | Type | Default |
|-----|------|---------|
| `mdtk` | String | `01237780` |
| `zone` | String | `25` |
| `src` | String | `3lflr3r` |
| `urlReferrer` | String | `https://test-app.digiteka.com/article` |
| `host` | String | `www.ultimedia.com` |

### "videofeed_prefs"

| Key | Type | Default |
|-----|------|---------|
| `mdtk` | String | `01573101` |
| `zoneId` | String? | null |
| `adUnitPath` | String? | null |
| `videoId` | String? | null |

### System SharedPreferences (IAB standard)

| Key | Type | Set by |
|-----|------|--------|
| `IABTCF_TCString` | String | Google UMP SDK automatically |

---

*Last updated: 2026-04-21 — reflects Android implementation as of that date.*
