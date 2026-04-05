package com.aamovies.aamovies.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.aamovies.aamovies.LoginActivity
import com.aamovies.aamovies.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tvName = view.findViewById<TextView>(R.id.tv_profile_name)
        val tvEmail = view.findViewById<TextView>(R.id.tv_profile_email)
        val tvWatchlistCount = view.findViewById<TextView>(R.id.tv_watchlist_count)
        val tvLikedCount = view.findViewById<TextView>(R.id.tv_liked_count)
        val btnLogout = view.findViewById<Button>(R.id.btn_logout)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            tvName.text = if (user.isAnonymous) "Guest User" else (user.displayName ?: "User")
            tvEmail.text = if (user.isAnonymous) "Anonymous" else (user.email ?: "—")

            FirebaseDatabase.getInstance().getReference("users/${user.uid}")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!isAdded) return
                        val watchlistCount = snapshot.child("watchlist").childrenCount
                        val likedCount = snapshot.child("liked").childrenCount
                        tvWatchlistCount.text = watchlistCount.toString()
                        tvLikedCount.text = likedCount.toString()
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
