package com.aamovies.aamovies

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogle: Button
    private lateinit var btnGuest: Button
    private lateinit var btnSignup: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvForgotPassword: TextView

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            goToMain()
            return
        }

        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        btnGoogle = findViewById(R.id.btn_google)
        btnGuest = findViewById(R.id.btn_guest)
        btnSignup = findViewById(R.id.tv_signup)
        progressBar = findViewById(R.id.progress_bar)
        tvForgotPassword = findViewById(R.id.tv_forgot_password)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        btnLogin.setOnClickListener { handleEmailLogin() }
        btnGoogle.setOnClickListener { handleGoogleLogin() }
        btnGuest.setOnClickListener { handleAnonymousLogin() }
        tvForgotPassword.setOnClickListener { handlePasswordReset() }
        btnSignup.setOnClickListener { handleEmailSignup() }
    }

    private fun handleEmailLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }
        showLoading(true)
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { goToMain() }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, e.message ?: "Login failed", Toast.LENGTH_LONG).show()
            }
    }

    private fun handleEmailSignup() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }
        showLoading(true)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { goToMain() }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, e.message ?: "Signup failed", Toast.LENGTH_LONG).show()
            }
    }

    private fun handleGoogleLogin() {
        showLoading(true)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun handleAnonymousLogin() {
        showLoading(true)
        auth.signInAnonymously()
            .addOnSuccessListener { goToMain() }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, e.message ?: "Guest login failed", Toast.LENGTH_LONG).show()
            }
    }

    private fun handlePasswordReset() {
        val email = etEmail.text.toString().trim()
        if (email.isEmpty()) {
            Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show()
            return
        }
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(this, "Password reset email sent!", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, e.message ?: "Failed to send reset email", Toast.LENGTH_LONG).show()
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                showLoading(false)
                Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { goToMain() }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, e.message ?: "Google auth failed", Toast.LENGTH_LONG).show()
            }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !loading
        btnGoogle.isEnabled = !loading
        btnGuest.isEnabled = !loading
    }
}
