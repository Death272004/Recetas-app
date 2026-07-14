package com.utp.recetaslid.data

import android.content.Context

// Maneja la sesion del usuario activo usando SharedPreferences
class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("sesion_recetas_lid", Context.MODE_PRIVATE)

    fun guardarSesion(id: Int, nombre: String, rol: String) {
        prefs.edit()
            .putInt("usuario_id", id)
            .putString("nombre", nombre)
            .putString("rol", rol)
            .apply()
    }

    fun getUsuarioId(): Int = prefs.getInt("usuario_id", -1)
    fun getNombre(): String = prefs.getString("nombre", "") ?: ""
    fun getRol(): String = prefs.getString("rol", "usuario") ?: "usuario"
    fun haySesion(): Boolean = getUsuarioId() != -1
    fun esAdmin(): Boolean = getRol() == "admin"

    fun actualizarNombre(nombre: String) {
        prefs.edit().putString("nombre", nombre).apply()
    }

    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }
}
