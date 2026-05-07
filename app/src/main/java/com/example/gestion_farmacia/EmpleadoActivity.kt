package com.example.gestion_farmacia

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class EmpleadoActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empleado)

        auth    = FirebaseAuth.getInstance()
        session = SessionManager(this)

        if (auth.currentUser == null) {
            irAlLogin()
            return
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        // Limpiar primero para evitar duplicados
        bottomNav.menu.clear()
        bottomNav.inflateMenu(R.menu.bottom_nav_menu_empleado)
        bottomNav.setupWithNavController(navController)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_cerrar_sesion -> {
                    AlertDialog.Builder(this)
                        .setTitle("Cerrar sesión")
                        .setMessage("¿Estás seguro que deseas salir?")
                        .setPositiveButton("Salir") { _, _ ->
                            auth.signOut()
                            session.cerrarSesion()
                            irAlLogin()
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                    true
                }
                else -> {
                    navController.navigate(item.itemId)
                    true
                }
            }
        }
    }

    private fun irAlLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}