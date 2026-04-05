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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()

        val etName = findViewById<EditText>(R.id.et_reg_name)
        val etEmail = findViewById<EditText>(R.id.et_reg_email)
        val etPassword = findViewById<EditText>(R.id.et_reg_password)
        val etConfirm = findViewById<EditText>(R.id.et_reg_confirm)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val tvLogin = findViewById<TextView>(R.id.tv_goto_login)
        val progressBar = findViewById<ProgressBar>(R.id.reg_progress)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirm = etConfirm.text.toString().trim()

            if (name.isEmpty()) { Toast.makeText(this, "Enter your name", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (email.isEmpty()) { Toast.makeText(this, "Enter your email", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (password.length < 6) { Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (password != confirm) { Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show(); return@setOnClickListener }

            btnRegister.isEnabled = false
            progressBar.visibility = View.VISIBLE

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    result.user?.updateProfile(profileUpdate)?.addOnCompleteListener {
                        Toast.makeText(this, "Account created! Welcome, $name", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                }
                .addOnFailureListener { e ->
                    btnRegister.isEnabled = true
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, e.message ?: "Registration failed", Toast.LENGTH_LONG).show()
                }
        }

        tvLogin.setOnClickListener { finish() }
    }
}
