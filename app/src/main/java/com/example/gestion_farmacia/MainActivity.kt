package com.example.gestion_farmacia

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // Mostrar email del usuario
        val tvBienvenida = findViewById<TextView>(R.id.tvBienvenida)
        tvBienvenida.text = "Bienvenido, ${auth.currentUser?.email}"

        // Navegar a Medicamentos
        findViewById<CardView>(R.id.cardMedicamentos).setOnClickListener {
            startActivity(Intent(this, MedicamentosActivity::class.java))
        }

        // Navegar a Proveedores
        findViewById<CardView>(R.id.cardProveedores).setOnClickListener {
            startActivity(Intent(this, ProveedoresActivity::class.java))
        }

        // Cerrar sesión
        findViewById<CardView>(R.id.cardCerrarSesion).setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}