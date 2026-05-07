package com.example.gestion_farmacia

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth    = FirebaseAuth.getInstance()
        db      = FirebaseFirestore.getInstance()
        session = SessionManager(this)

        // Si ya hay sesión activa con rol guardado, ir directo
        if (auth.currentUser != null && session.getRol().isNotEmpty()) {
            navegarSegunRol(session.getRol())
            return
        }

        val etEmail    = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin   = findViewById<Button>(R.id.btnLogin)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        btnLogin.setOnClickListener {
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnLogin.isEnabled = false

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val uid = authResult.user?.uid ?: ""
                    // Leer rol desde Firestore
                    db.collection("usuarios").document(uid).get()
                        .addOnSuccessListener { doc ->
                            progressBar.visibility = View.GONE
                            if (doc.exists()) {
                                val rol    = doc.getString("rol") ?: SessionManager.ROL_EMPLEADO
                                val nombre = doc.getString("nombre") ?: email
                                val activo = doc.getBoolean("activo") ?: true

                                if (!activo) {
                                    auth.signOut()
                                    btnLogin.isEnabled = true
                                    Toast.makeText(this, "Tu cuenta está desactivada. Contacta al administrador.", Toast.LENGTH_LONG).show()
                                    return@addOnSuccessListener
                                }

                                session.guardarSesion(email, rol, nombre)
                                navegarSegunRol(rol)
                            } else {
                                // Usuario existe en Auth pero no en Firestore
                                // Podría ser el admin inicial: tratarlo como admin
                                session.guardarSesion(email, SessionManager.ROL_ADMIN, email)
                                navegarSegunRol(SessionManager.ROL_ADMIN)
                            }
                        }
                        .addOnFailureListener { e ->
                            progressBar.visibility = View.GONE
                            btnLogin.isEnabled = true
                            Toast.makeText(this, "Error al obtener datos: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                    btnLogin.isEnabled = true
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun navegarSegunRol(rol: String) {
        val intent = when (rol) {
            SessionManager.ROL_ADMIN    -> Intent(this, MainActivity::class.java)
            else                        -> Intent(this, EmpleadoActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}
