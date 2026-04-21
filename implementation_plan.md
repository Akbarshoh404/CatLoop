# CatLoop Android Game — Implementation Plan

A complete, production-ready cat-themed Spiky Fish 3 clone using pure Jetpack Compose + Canvas. No game engines.

## Project Context

The existing project is a blank Android Studio scaffold at `D:\Projects\Mobile Development\CatLoop\` with package `uz.angrykitten.catloop` (using existing namespace, not `com.angrykirren.catloop`). The Compose BOM version is `2024.09.00`. We need to add Navigation, ViewModel, and DataStore dependencies to `libs.versions.toml` + `build.gradle.kts`.

> [!IMPORTANT]
> The existing package name is `uz.angrykitten.catloop` — all files will use this package. The manifest `screenOrientation="portrait"` will be added.

---

## Proposed Changes

### 1. Gradle & Build Config

#### [MODIFY] [libs.versions.toml](file:///D:/Projects/Mobile%20Development/CatLoop/gradle/libs.versions.toml)
Add versions and libraries for:
- `navigation-compose:2.7.7`
- `lifecycle-viewmodel-compose:2.8.7`
- `datastore-preferences:1.1.1`

#### [MODIFY] [build.gradle.kts](file:///D:/Projects/Mobile%20Development/CatLoop/app/build.gradle.kts)
Wire new library references to `dependencies {}` block.

---

### 2. AndroidManifest

#### [MODIFY] [AndroidManifest.xml](file:///D:/Projects/Mobile%20Development/CatLoop/app/src/main/AndroidManifest.xml)
- Add `android:screenOrientation="portrait"` to the activity
- Add `android:windowSoftInputMode="stateHidden"` for clean game UX

---

### 3. Theme Files

#### [MODIFY] [Color.kt](file:///D:/Projects/Mobile%20Development/CatLoop/app/src/main/java/uz/angrykitten/catloop/ui/theme/Color.kt)
Replace placeholder colors with the full `CatLoopColors` palette.

#### [MODIFY] [Theme.kt](file:///D:/Projects/Mobile%20Development/CatLoop/app/src/main/java/uz/angrykitten/catloop/ui/theme/Theme.kt)
Update to use `CatLoopColors`, apply a single light-mode-only theme since the game has a fixed beige background.

#### [MODIFY] [Type.kt](file:///D:/Projects/Mobile%20Development/CatLoop/app/src/main/java/uz/angrykitten/catloop/ui/theme/Type.kt)
Add custom typography definitions.

---

### 4. Game Data Layer

#### [NEW] GameState.kt — `game/GameState.kt`
Data class holding all runtime game state fields.

#### [NEW] HighScoreRepository.kt — `data/HighScoreRepository.kt`
DataStore Preferences wrapper: `getHighScore(): Flow<Int>`, `saveHighScore(score: Int)`.

---

### 5. Game Logic

#### [NEW] CollisionDetector.kt — `game/CollisionDetector.kt`
- `checkSpikeCollision(catAngle, spike angles, ringAngle, tolerance)` — angular proximity check
- `checkOrbCollection(catPos, orbPos, sumRadii)` — Euclidean distance check

#### [NEW] GameEngine.kt — `game/GameEngine.kt`
`GameViewModel : ViewModel()` with:
- `StateFlow<GameState>` exposing game state
- `update(deltaMs: Float)` — advances ring rotation, checks collisions, handles orb spawning, increments difficulty
- `moveCat(deltaAngle: Float)` — adjusts cat angular velocity
- `resetGame()` — resets to initial state
- `collectHighScore` from `HighScoreRepository`

---

### 6. Canvas Drawing

#### [NEW] CatDrawing.kt — `ui/components/CatDrawing.kt`
`DrawScope.drawCatFace(center, size, facingAngle)` — draws the orange angry cat head (ears, eyes, eyebrows, nose, whiskers, tabby stripes).

#### [NEW] LogoDrawing.kt — `ui/components/LogoDrawing.kt`
`DrawScope.drawCatLogo(center, size)` — large sticker-style cat head + red heart for splash & menu screens.

#### [NEW] PawBackground.kt — `ui/components/PawBackground.kt`
`DrawScope.drawPawPrints(size, alpha)` — draws 8 scattered faint paw prints.

#### [NEW] GameRenderer.kt — `game/GameRenderer.kt`
Complete set of `DrawScope` extension functions:
- `drawRing(center, radius, rotationAngle, strokeWidth)`
- `drawSpike(center, ringRadius, absoluteSpikeAngle, spikeSize)`
- `drawCat(center, ringRadius, catAngle, catSize)`
- `drawOrb(center, angle, distanceFraction, orbRadius)`
- `drawScoreText(score, canvasSize)` with white outline trick

---

### 7. Navigation

#### [NEW] NavGraph.kt — `navigation/NavGraph.kt`
NavHost with routes: `"splash"`, `"menu"`, `"game"`, `"gameover/{score}"`.
Transitions: `slideInHorizontally` / `fadeIn` combos for each route.

---

### 8. Screens

#### [NEW] SplashScreen.kt — `screens/SplashScreen.kt`
- Full beige Canvas background
- `drawCatLogo()` centered
- "CatLoop" title + "AngryKitten" subtitle
- `LaunchedEffect`: `delay(2500)` then navigate to `"menu"`

#### [NEW] MenuScreen.kt — `screens/MenuScreen.kt`
- Beige background with paw print Canvas overlay
- Cat logo top half
- "PLAY" dark-red rounded button
- "HIGH SCORE" display (reads from DataStore via ViewModel)
- Fade-in `AnimatedVisibility` on entry

#### [NEW] GameScreen.kt — `screens/GameScreen.kt`
- Full-screen `Canvas` composable
- `LaunchedEffect` game loop with `withFrameNanos`
- Left/Right arrow `Button` overlays (bottom corners)
- `pointerInput` drag gesture on canvas
- Observes `gameState.gameOver` → navigate to `"gameover/{score}"`

#### [NEW] GameOverScreen.kt — `screens/GameOverScreen.kt`
- Beige background
- "GAME OVER" in dark red
- Score + Best score display
- "🎉 NEW BEST!" bouncing animation if new high score
- "PLAY AGAIN" + "MAIN MENU" buttons
- Slide-in animation from bottom
- Saves high score via ViewModel → Repository

---

### 9. MainActivity

#### [MODIFY] [MainActivity.kt](file:///D:/Projects/Mobile%20Development/CatLoop/app/src/main/java/uz/angrykitten/catloop/MainActivity.kt)
Replace boilerplate with `NavGraph` host, applying `CatLoopTheme`.

---

## Verification Plan

### Automated
- Run `./gradlew assembleDebug` to verify the project compiles without errors.

### Manual
- Install APK on emulator/device and verify:
  1. Splash → Menu → Game → GameOver transitions work
  2. Ring rotates, cat moves with arrow buttons and drag
  3. Spikes kill cat, orbs increment score
  4. High score persists after app restart
  5. Portrait lock is enforced
