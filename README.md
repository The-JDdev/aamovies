# AAMovies 2.0 — Native Android App

A full-featured movie streaming directory app built with **100% native Android** (XML + Kotlin).
No WebView for UI. Firebase Realtime Database backend.

## Features
- Browse movies: trending, pinned, latest
- Discover by Category / Genre / Year (horizontal chip filters)
- Movie detail: poster, description, screenshots gallery, download links
- Liked Movies — heart any movie, view all liked from the overflow menu
- Watchlist — save movies to watch later
- Ad redirect support (configure your own ad URLs in `AdManager.kt`)
- Background popup ad WebView (optional — configure URL in `AdManager.kt`)
- Register / Login / Profile (Firebase Auth)

## Setup

### 1. Firebase
- Create a Firebase project at https://console.firebase.google.com
- Add an Android app with package name `com.aamovies.aamovies`
- Download `google-services.json` and place it at `app/google-services.json`

### 2. Ad Configuration (optional)
Open `app/src/main/java/com/aamovies/aamovies/util/AdManager.kt` and fill in:
```kotlin
private const val AD_REDIRECT_URL = "YOUR_AD_REDIRECT_URL"
private const val AD_POPUP_SCRIPT_URL = "YOUR_POPUP_SCRIPT_URL"
```

### 3. Build
```bash
./gradlew assembleRelease
```

## Project Structure
```
app/src/main/java/com/aamovies/aamovies/
  ├── fragment/       — HomeFragment, ProfileFragment, WatchlistFragment
  ├── adapter/        — MovieAdapter, ChipAdapter, DownloadAdapter, ScreenshotAdapter
  ├── model/          — Movie, DownloadLink
  ├── util/           — AdManager
  ├── LoginActivity, RegisterActivity
  ├── MovieDetailActivity
  ├── FilteredMoviesActivity
  └── LikedMoviesActivity
```

## License
MIT
