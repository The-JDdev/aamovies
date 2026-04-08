<div align="center">

<img src="https://readme-typing-svg.herokuapp.com/?font=Fira+Code&weight=800&size=40&pause=1000&color=00A8FF&center=true&vCenter=true&width=800&height=100&lines=AAMovies+v7.2+Native+Universe;The+4.4MB+Speed+Monster;100%25+Kotlin+%2B+Firebase" alt="Typing SVG" />

<p align="center">
  <img src="https://img.shields.io/badge/Version-v7.2-00a8ff?style=for-the-badge&logo=android&logoColor=white" alt="Version">
  <img src="https://img.shields.io/badge/Size-4.4%20MB-success?style=for-the-badge&logo=rocket" alt="Size">
  <img src="https://img.shields.io/badge/Downloads-1K%2B-ff0000?style=for-the-badge&logo=flame&logoColor=white" alt="Trending">
  <img src="https://img.shields.io/badge/Architecture-100%25%20Native-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Database-Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" alt="Firebase">
</p>

> 🌌 **Welcome to the revolution of mobile entertainment.** AAMovies is a 100% pure native, ultra-optimized streaming directory. We rebuilt the entire ecosystem from scratch to deliver absolute speed, impenetrable security, and a cinematic UI/UX—all packed into an unbelievable **4.4 MB** footprint!

<br>

<table>
  <tr>
    <td align="center">
      <h2>🔥 EXPERIENCE THE MASS ACTION NOW 🔥</h2>
      <a href="https://apkpure.com/p/com.aamovies.aamovies">
        <img src="https://img.shields.io/badge/DOWNLOAD_ON-APKPure-1f2937?style=for-the-badge&logo=android&logoColor=24b672" height="60" alt="Get it on APKPure" />
      </a>
      <br>
      <i>Join thousands of users globally. Zero lag. Pure Cinema.</i>
    </td>
  </tr>
</table>

<br><br>
</div>

---

## 🔔 🆕 WHAT'S NEW IN v7.0 (The Global Update)

<table>
  <tr>
    <td width="50%" align="center">
      <h3>🚀 4.4 MB Micro-Footprint</h3>
      <p>Unprecedented optimization. The entire cinematic universe shrunk to 4.4 MB for instant downloads and zero lag.</p>
    </td>
    <td width="50%" align="center">
      <h3>📲 Live FCM Notifications</h3>
      <p>Real-time push alerts! The moment a new movie is uploaded by the Admin, users get a system notification with the Poster and Title.</p>
    </td>
  </tr>
</table>

---

## 🍿 FOR THE USERS (The Cinematic Experience)

Say goodbye to laggy web-views. This is a premium, high-octane native experience designed for true movie buffs.

### 🎨 ⌈ Premium Visual Identity ⌋
* 🌑 **True Dark Theme:** Built with a pure dark UI (`#1E1E2E`) optimized perfectly for AMOLED screens to save battery and protect eyes.
* 🔵 **Signature Blue Borders:** Every movie and series poster features a glowing, solid blue border for a distinct, high-end premium look.
* 🖼️ **Dynamic Post Details:** A massive horizontal banner background combined with a left-aligned vertical poster overlay.
* 🏷️ **Vibrant Badges:** Colorful tag badges clearly indicating Language, Genre, Rating, Quality, and Pinned/Trending status.
* 📱 **Dynamic Screenshot Grid:** The layout calculates array length on the fly: 1 image = full width; ≥ 2 images = beautiful 2-column grid.

### 🧭 ⌈ Smart Browsing & Navigation ⌋
* 🔘 **Bottom Navigation Bar:** Seamlessly switch between 5 core tabs: **Home, Search, Trending, Watchlist, Profile**.
* 🍔 **Slide-Out Drawer:** A clean hamburger menu housing *Contact Us, Global Search, Watchlist,* and *Liked Movies*.
* ⚡ **Advanced Pagination:** The Latest Release section loads lightning-fast with strict **32-items-per-page** pagination (e.g., `<kbd>1</kbd> | <kbd>2</kbd> | <kbd>3</kbd> ...`).
* 📌 **LIFO Algorithm (Pinned & Trending):** The absolute newest blockbusters are **Pinned** at the very top. The **Trending** section features a smooth horizontal scroll of the top 15 hottest items.
* 🔍 **Smart Search:** Toggle instantly between `[Movies]` and `[Series]` chips to filter results instantly, or search the global database.

### ❤️ ⌈ Your Personal Empire ⌋
* 🎬 Build your library with one tap. Access your **Liked Movies** and **Watchlist** from your Profile, beautifully displayed in a visual 2-column poster grid (no boring text lists!).
* 🌍 **Global Socials:** Dynamic settings fetch our Telegram and Facebook community links, placed elegantly at the bottom of any movie page.

---

## 💻 FOR THE DEVELOPERS (Architecture & Logic)

This project is a masterclass in Android optimization, serverless architecture, and smart monetization.

### 🛡️ ⌈ Anti-Modding & Security Shield ⌋
* 🔐 **APK Signature Verification:** Hardcoded hash matching logic. If the APK is unpacked, modified, and re-signed by any third party, the app immediately crashes.
* 🚫 **Tamper Detection:** Basic Root and Emulator detection enabled.
* 🗜️ **Aggressive Obfuscation:** Strict ProGuard/R8 rules applied to shrink the code, hide core logic, and achieve the 4.4 MB size.

### 💰 ⌈ Mastermind Monetization (AdManager.kt) ⌋
A highly sophisticated, non-intrusive custom ad flow built to maximize revenue without destroying user experience:

1. **Invisible Web Layer:** An invisible `WebView` loads in the background on app launch, injecting a JS popup script after a 3-second delay to conditionally bypass ad-blockers.
2. **The "Toll Plaza" Flow:** * User clicks a Movie or Download button ➔ Target is stored in `SharedPreferences`.
   * App fires `Intent.ACTION_VIEW` for the Ad Direct Link.
   * User presses 'Back' ➔ `onResume` triggers the logic to seamlessly auto-navigate to the actual Movie Detail or Download Link.

---

## 🛠️ SETUP & INSTALLATION

### 📦 1. Firebase Configuration
Connect to the serverless backend.
- Create a project at [Firebase Console](https://console.firebase.google.com)
- Add an Android app with package name: `com.aamovies.aamovies`
- Download `google-services.json` and place it in the `app/` directory.

### 💸 2. Ad Configuration (Optional)
Inject your monetization links directly into the manager file. Open `util/AdManager.kt` and update:

<pre><code>private const val AD_REDIRECT_URL = "YOUR_AD_REDIRECT_URL"
private const val AD_POPUP_SCRIPT_URL = "YOUR_POPUP_SCRIPT_URL"</code></pre>

### 🔨 3. Build & Compile
Generate the final, optimized APK using Gradle:

<pre><code>./gradlew assembleRelease</code></pre>

---

<div align="center">
  <h3><b>📂 CORE DIRECTORY STRUCTURE</b></h3>
  <pre><code>Home ➔ Search ➔ Trending ➔ Watchlist ➔ Profile</code></pre>
  <br>
  <img src="https://img.shields.io/badge/Engineered_by-JD_Vijay-000000?style=for-the-badge&logo=github&logoColor=white" alt="JD Vijay">
  <p><i>The Native Revolution</i></p>
</div>
