package com.aamovies.aamovies.bridge

import android.content.Intent
import android.os.Handler
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

/**
 * AuthBridge — ALL Firebase Authentication logic lives here.
 * HTML must NOT use the Firebase JS SDK for auth.
 * HTML calls: AndroidAuth.loginWithEmail(email, pass), AndroidAuth.loginWithGoogle(), etc.
 * Native calls back: window.onNativeAuthSuccess(userJson) / window.onNativeAuthError(msg)
 */
class AuthBridge(
    private val activity: AppCompatActivity,
    private val webView: WebView,
    private val handler: Handler,
    private val googleLauncher: ActivityResultLauncher<Intent>
) {
    private val auth = FirebaseAuth.getInstance()

    // ──────────────────────────────────────────────────────────────────────────
    // Email / Password Auth
    // ──────────────────────────────────────────────────────────────────────────

    @JavascriptInterface
    fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->
                callbackSuccess(result.user!!)
            }
            .addOnFailureListener { e ->
                callbackError(e.message)
            }
    }

    @JavascriptInterface
    fun registerWithEmail(email: String, password: String, displayName: String) {
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->
                val user = result.user!!
                val update = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .build()
                user.updateProfile(update).addOnCompleteListener {
                    callbackSuccess(user)
                }
            }
            .addOnFailureListener { e ->
                callbackError(e.message)
            }
    }

    @JavascriptInterface
    fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email.trim())
            .addOnSuccessListener {
                js("window.onNativePasswordResetSent()")
            }
            .addOnFailureListener { e ->
                callbackError(e.message)
            }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Google Sign-In  (triggers Activity result — handled in MainActivity)
    // ──────────────────────────────────────────────────────────────────────────

    @JavascriptInterface
    fun loginWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(activity, gso)
        client.signOut().addOnCompleteListener {
            handler.post { googleLauncher.launch(client.signInIntent) }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Anonymous Sign-In
    // ──────────────────────────────────────────────────────────────────────────

    @JavascriptInterface
    fun loginAnonymously() {
        auth.signInAnonymously()
            .addOnSuccessListener { result ->
                callbackSuccess(result.user!!)
            }
            .addOnFailureListener { e ->
                callbackError(e.message)
            }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Upgrade anonymous account → email/password
    // ──────────────────────────────────────────────────────────────────────────

    @JavascriptInterface
    fun linkAnonymousWithEmail(email: String, password: String) {
        val user = auth.currentUser ?: return callbackError("No signed-in user")
        val credential = EmailAuthProvider.getCredential(email.trim(), password)
        user.linkWithCredential(credential)
            .addOnSuccessListener { result ->
                callbackSuccess(result.user!!)
            }
            .addOnFailureListener { e ->
                callbackError(e.message)
            }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Current user / Logout
    // ──────────────────────────────────────────────────────────────────────────

    @JavascriptInterface
    fun getCurrentUser(): String {
        val user = auth.currentUser ?: return "null"
        return buildUserJson(user)
    }

    @JavascriptInterface
    fun isLoggedIn(): Boolean = auth.currentUser != null

    @JavascriptInterface
    fun isAnonymous(): Boolean = auth.currentUser?.isAnonymous == true

    @JavascriptInterface
    fun logout() {
        auth.signOut()
        js("window.onNativeLogout()")
    }

    @JavascriptInterface
    fun deleteAccount() {
        auth.currentUser?.delete()
            ?.addOnSuccessListener { js("window.onNativeLogout()") }
            ?.addOnFailureListener { e -> callbackError(e.message) }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    private fun callbackSuccess(user: FirebaseUser) {
        val json = buildUserJson(user)
        js("window.onNativeAuthSuccess($json)")
    }

    private fun callbackError(msg: String?) {
        val safeMsg = (msg ?: "Unknown error").escaped()
        js("window.onNativeAuthError('$safeMsg')")
    }

    internal fun buildUserJson(user: FirebaseUser): String = """{
        "uid":"${user.uid}",
        "email":"${user.email ?: ""}",
        "displayName":"${(user.displayName ?: "").escaped()}",
        "isAnonymous":${user.isAnonymous},
        "photoUrl":"${user.photoUrl ?: ""}",
        "emailVerified":${user.isEmailVerified}
    }"""

    private fun js(script: String) {
        handler.post { webView.evaluateJavascript(script, null) }
    }

    private fun String.escaped() = replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
}
