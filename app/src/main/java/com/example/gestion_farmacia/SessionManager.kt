package com.example.gestion_farmacia

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("FarmaciaSession", Context.MODE_PRIVATE)

    companion object {
        const val ROL_ADMIN    = "admin"
        const val ROL_EMPLEADO = "empleado"
        private const val KEY_ROL    = "rol"
        private const val KEY_EMAIL  = "email"
        private const val KEY_NOMBRE = "nombre"
    }

    fun guardarSesion(email: String, rol: String, nombre: String = "") {
        prefs.edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_ROL, rol)
            .putString(KEY_NOMBRE, nombre)
            .apply()
    }

    fun getRol(): String = prefs.getString(KEY_ROL, "") ?: ""
    fun getEmail(): String = prefs.getString(KEY_EMAIL, "") ?: ""
    fun getNombre(): String = prefs.getString(KEY_NOMBRE, "") ?: ""

    fun esAdmin(): Boolean = getRol() == ROL_ADMIN
    fun esEmpleado(): Boolean = getRol() == ROL_EMPLEADO

    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }
}
