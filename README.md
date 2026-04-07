<div align="center">



# 🎬 AAMovies 2.0 (v6.0 → v7.1) 🎬

### *The Ultimate Native Cinematic Universe*



<p align="center">

  <img src="https://img.shields.io/badge/Version-v6.3-00a8ff?style=for-the-badge&logo=android" alt="Version">

  <img src="https://img.shields.io/badge/Size-4.7%20MB-success?style=for-the-badge" alt="Size">

  <img src="https://img.shields.io/badge/Language-Kotlin%20%2B%20XML-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin">

  <img src="https://img.shields.io/badge/Backend-Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" alt="Firebase">

</p>



Welcome to the revolution of mobile entertainment. **AAMovies 2.0** is a 100% pure native, ultra-optimized streaming directory. We rebuilt the entire ecosystem from scratch to deliver absolute speed, impenetrable security, and a cinematic UI/UX—all packed into an unbelievable **4.7 MB** footprint.



<br>



<a href="https://apkpure.com/p/com.aamovies.aamovies">

  <img src="https://img.shields.io/badge/Get_it_on-APKPure-1f2937?style=for-the-badge&logo=android&logoColor=24b672" height="45" alt="Get it on APKPure" />

</a>



<br><br>

</div>



---



## 🍿 For The Users (Features & UI/UX)



Say goodbye to laggy web-views. This is a premium, high-octane native experience designed for true movie buffs.



### 🌌 Premium Cinematic Design

* **True Dark Theme:** Built with a dark UI (`#1E1E2E`) optimized for AMOLED screens.

* **Signature Blue Borders:** Every movie and series poster features a glowing, solid blue border for a distinct, premium look.

* **Dynamic Post Details:** A massive horizontal banner background combined with a left-aligned vertical poster overlay, complete with colorful tag badges.



### 🚀 Smart Browsing & Navigation

* **Bottom Navigation:** Seamlessly switch between 5 core tabs: **Home, Search, Trending, Watchlist, Profile**.

* **Slide-Out Drawer:** A clean hamburger menu housing *Contact Us, Global Search, Watchlist,* and *Liked Movies*.

* **Advanced Pagination:** The Latest Release section loads lightning-fast with strict **32-items-per-page** pagination (e.g., `1 | 2 | 3 | ... | 82`).

* **LIFO Trending & Pinned Posts:** Blockbusters are Pinned at the very top. The Trending section features a smooth horizontal scroll of the top 15 hottest items.



### ❤️ Your Personal Empire

* Build your library with one tap. Access your **Liked Movies** and **Watchlist** from your Profile, beautifully displayed in a 2-column poster grid.

* **Global Socials:** Connect directly to our Telegram and Facebook communities from the bottom of any movie page.



---



## 💻 For The Developers (Architecture & Logic)



This project is a masterclass in Android optimization, security, and smart monetization.



### 🏗️ Core Architecture (User & Admin Apps)

* **Tech Stack:** 100% Native Android (Platform `android-34`, Build Tools `34.0.0`).

* **Backend:** Firebase Realtime Database (`aamovies-12d36-default-rtdb`).

* **CI/CD:** Automated GitHub push scripts handle 5 build flavors and clean sensitive files before commit.



### 🛡️ Anti-Modding & Security Shield

* **APK Signature Verification:** Hardcoded hash matching. If the APK is modified and re-signed, the app crashes.

* **Obfuscation:** Strict ProGuard/R8 rules applied to shrink code to **4.7 MB**.



### 💰 Mastermind Monetization (`AdManager.kt`)

1. **Invisible Web Layer:** An invisible `WebView` injects a JS popup script after a 3-second delay.

2. **The "Toll Plaza" Flow:** * `Click` ➔ Stores Target in `SharedPreferences`.

   * ➔ Fires `Intent.ACTION_VIEW` for Ad Link.

   * ➔ User presses 'Back' ➔ `onResume` triggers `consumePending()`.

   * ➔ Auto-navigates to actual Movie Detail or Download Link.



---



## 🛠️ Setup & Installation



### 1. Firebase

- Create a project at https://console.firebase.google.com

- Package name: `com.aamovies.aamovies`

- Download `google-services.json` to `app/`



### 2. Ad Configuration (optional)

Open `util/AdManager.kt` and fill in:

```kotlin

private const val AD_REDIRECT_URL = "YOUR_AD_REDIRECT_URL"

private const val AD_POPUP_SCRIPT_URL = "YOUR_POPUP_SCRIPT_URL"

```



### 3. Build & Compile

Generate the release APK via Gradle:

```bash

./gradlew assembleRelease

```



---



## 📂 Project Structure



```text

app/src/main/java/com/aamovies/aamovies/

  ├── fragment/       — Home, Profile, Watchlist

  ├── adapter/        — Movie, Chip, Download, Screenshot

  ├── model/          — Movie, DownloadLink

  ├── util/           — AdManager (Core monetization)

  ├── LoginActivity, RegisterActivity

  ├── MovieDetailActivity, FilteredMoviesActivity, LikedMoviesActivity

```



<br>



<div align="center">

  <p><i>Engineered & Designed by <b>JD Vijay</b> | The Native Revolution</i></p>

</div>
