<div align="center">

<img src="https://img.shields.io/badge/AAMOVIES_2.0-VIP_EDITION-FF0000?style=for-the-badge&logo=themoviedatabase&logoColor=white" alt="AAMovies Logo"/>

# 🎬 AAMOVIES 2.0 : THE CINEMATIC ECOSYSTEM
**The Ultimate 100% Native Android Movie Universe (v6.3)**

[![Platform](https://img.shields.io/badge/Platform-Android_34-3DDC84?style=flat-square&logo=android&logoColor=white)](#)
[![Architecture](https://img.shields.io/badge/Architecture-100%25_Native_Kotlin-0095D5?style=flat-square&logo=kotlin&logoColor=white)](#)
[![Backend](https://img.shields.io/badge/Backend-Firebase_RTDB-FFCA28?style=flat-square&logo=firebase&logoColor=black)](#)
[![Status](https://img.shields.io/badge/Status-Production_Ready-success?style=flat-square)](#)
[![Developer](https://img.shields.io/badge/Developer-JD_Vijay-8A2BE2?style=flat-square&logo=github&logoColor=white)](https://github.com/The-JDdev)

> *Forget the lag. Forget the webviews. AAMovies 2.0 is an adrenaline-fueled, 100% Native Android masterpiece engineered to run flawlessly even on a 2GB RAM device. This is the future of mobile entertainment.*

---
</div>

## 📥 DIRECT APK DOWNLOADS (RELEASE v6.3)

> **🚨 VIP ACCESS RELEASES**
> Choose your architecture below to download the compiled, signed, and ready-to-use application.

<table>
  <tr>
    <th align="left">📦 Package Flavor</th>
    <th align="left">📱 Target Device / Store</th>
    <th align="center">🔗 Download Link</th>
  </tr>
  <tr>
    <td><b>AAMovies Direct</b></td>
    <td>Universal Android (Default)</td>
    <td align="center"><a href="https://github.com/The-JDdev/aamovies/releases/tag/v6.3"><kbd>&nbsp;↓ Download APK&nbsp;</kbd></a></td>
  </tr>
  <tr>
    <td><b>AAMovies Samsung</b></td>
    <td>Galaxy Store Optimized</td>
    <td align="center"><a href="https://github.com/The-JDdev/aamovies/releases/tag/v6.3"><kbd>&nbsp;↓ Download APK&nbsp;</kbd></a></td>
  </tr>
  <tr>
    <td><b>AAMovies Amazon</b></td>
    <td>Amazon Fire Devices</td>
    <td align="center"><a href="https://github.com/The-JDdev/aamovies/releases/tag/v6.3"><kbd>&nbsp;↓ Download APK&nbsp;</kbd></a></td>
  </tr>
  <tr>
    <td><b>AAMovies Huawei</b></td>
    <td>AppGallery Optimized</td>
    <td align="center"><a href="https://github.com/The-JDdev/aamovies/releases/tag/v6.3"><kbd>&nbsp;↓ Download APK&nbsp;</kbd></a></td>
  </tr>
  <tr>
    <td><b>AAMovies APKPure</b></td>
    <td>Third-Party Stores</td>
    <td align="center"><a href="https://github.com/The-JDdev/aamovies/releases/tag/v6.3"><kbd>&nbsp;↓ Download APK&nbsp;</kbd></a></td>
  </tr>
</table>

---

## 🌟 SUPERPOWER INVENTORY

### ⚡ THE NATIVE ENGINE
* 🟢 **Pure XML & Kotlin:** Built entirely with Android standard components (`RecyclerView`, `CardView`). Zero WebView UI wrappers.
* 🟢 **Ghost Authentication:** A frictionless login ecosystem powered natively by Firebase (Google Sign-In, Email/Password, & Anonymous Ghost Login).
* 🟢 **Silent Push (FCM):** Background topic subscription to `aamovies_all_users` on launch. Users receive blockbusters without even opening the app.
* 🟢 **Netflix-Grade Splash:** A premium 3-second zoom-and-fade XML animation that sets a cinematic tone before the UI even loads.

### 🍿 THE DISCOVERY MATRIX
* 🔵 **Dynamic Bottom Filters:** Horizontal, thumb-friendly chip sliders for **Category**, **Genre**, and **Release Year** placed intuitively at the bottom of the Home Screen.
* 🔵 **My Favorites Ecosystem:** A dedicated vault saving favorite movies directly to the user's Firebase UID (`users/{uid}/liked_movies`).
* 🔵 **Intelligent Hierarchy:** 📌 **Pinned** blockbusters command the top, followed by 🔥 **Trending** scrolls, finishing with chronological latest releases.
* 🔵 **Native Support Routing:** `Intent.ACTION_SENDTO` triggers instant "Contact Us" emails natively.

### 💸 THE INVISIBLE MONETIZATION (AdManager.kt)
> **💰 The "Ghost" Strategy:** Maximizing revenue while maintaining a VIP user experience.

* 🟣 **The Ghost Browser:** Silently executes CPM popup scripts inside an invisible background WebView. Free impressions without ruining the UI.
* 🟣 **Auto-Destiny Intents:** Clicking a movie card natively fires a Direct Link intent. When the user hits `BACK`, the app **automatically intercepts** and navigates them to their chosen movie. 100% Native, 100% Profitable.

---

## 🗄️ THE ARCHITECTURE BLUEPRINT

~~~text
📦 AAMOVIES_FIREBASE_RTDB
 ┣ 📂 movies
 ┃ ┗ 📂 {movieId}
 ┃   ┣ 🏷️ title, year, category, genre, type
 ┃   ┣ 🎛️ pinned, trending
 ┃   ┗ 🖼️ poster, screenshots, downloadLinks
 ┣ 📂 categories
 ┃ ┗ 📂 {categoryId} ➡️ name
 ┗ 📂 users
   ┗ 📂 {uid}
     ┗ 📂 liked_movies
       ┗ 📂 {movieId} ➡️ title, likedAt
~~~

---

## ⚙️ SYSTEM DEPLOYMENT (STRICT PROTOCOL)

<div align="center">
  <table>
    <tr>
      <td><b>⚠️ CRITICAL SECURITY PROTOCOL</b><br/>The <code>google-services.json</code> and <code>aamovies.keystore</code> files are <b>strictly isolated</b> via <code>.gitignore</code>. They MUST NEVER be pushed to a public repository. Monetization URLs are intentionally blanked in the public source.</td>
    </tr>
  </table>
</div>

### 🛠️ Step-by-Step Injection
**1. Clone the Core:**
~~~bash
git clone https://github.com/The-JDdev/aamovies.git
~~~

**2. Inject the Soul:** Secure your `google-services.json` from the Firebase Console and place it into the `app/` directory.

**3. Arm the Monetization:** Open `util/AdManager.kt` locally. Replace the `TODO` markers with your active CPM Direct Links and Popup URLs.

**4. Ignite the Build:** Open Android Studio, sync Gradle, and execute the Signed APK generation using your isolated keystore.

<div align="center">
  <br/>
  <b>🎬 Architected, Directed, and Engineered by <a href="https://github.com/The-JDdev">JD Vijay</a> 🎬</b>
</div>
