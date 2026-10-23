package com.example.ecomer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ecomer.databinding.ActivityLoginBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Nếu đã đăng nhập trước đó → vào thẳng MainActivity
        if (auth.currentUser != null) {
            navigateToMain()
            return
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener { login() }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Nút điền nhanh tài khoản Demo Admin & User
        binding.btnQuickAdmin.setOnClickListener {
            binding.edtEmail.setText("admin@ecomer.com")
            binding.edtPassword.setText("123456")
        }

        binding.btnQuickUser.setOnClickListener {
            binding.edtEmail.setText("user@ecomer.com")
            binding.edtPassword.setText("123456")
        }
    }

    private fun login() {
        val email = binding.edtEmail.text.toString().trim()
        val password = binding.edtPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.msg_fill_all), Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                navigateToMain()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Đăng nhập thất bại: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                setLoading(false)
            }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun setLoading(loading: Boolean) {
        binding.apply {
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            btnLogin.isEnabled = !loading
        }
    }
}
