package com.example.ecomer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ecomer.databinding.ActivityRegisterBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener { register() }
        binding.tvLogin.setOnClickListener { finish() }
    }

    private fun register() {
        val email = binding.edtRegEmail.text.toString().trim()
        val password = binding.edtRegPassword.text.toString().trim()
        val confirmPassword = binding.edtRegConfirmPassword.text.toString().trim()
        val role = if (binding.rbAdmin.isChecked) "admin" else "user"

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, getString(R.string.msg_fill_all), Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, getString(R.string.msg_password_mismatch), Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Mật khẩu tối thiểu 6 ký tự!", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                val userData = hashMapOf(
                    "email" to email,
                    "role" to role
                )
                db.collection("users").document(uid).set(userData)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            getString(R.string.msg_register_success),
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finishAffinity()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Lỗi lưu thông tin: ${e.message}", Toast.LENGTH_SHORT).show()
                        setLoading(false)
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Đăng ký thất bại: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                setLoading(false)
            }
    }

    private fun setLoading(loading: Boolean) {
        binding.apply {
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            btnRegister.isEnabled = !loading
        }
    }
}
