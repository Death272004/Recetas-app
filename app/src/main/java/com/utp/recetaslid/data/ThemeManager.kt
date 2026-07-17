package com.utp.recetaslid.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {
    private const val PREFS = "preferencias_app"
    private const val KEY_MODO_OSCURO = "modo_oscuro"

    fun aplicarModoGuardado(context: Context) {
        AppCompatDelegate.setDefaultNightMode(
            if (esModoOscuro(context)) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun esModoOscuro(context: Context): Boolean {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_MODO_OSCURO, false)
    }

    fun alternarModo(context: Context): Boolean {
        val nuevoModoOscuro = !esModoOscuro(context)
        establecerModo(context, nuevoModoOscuro)
        return nuevoModoOscuro
    }

    fun establecerModo(context: Context, modoOscuro: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_MODO_OSCURO, modoOscuro)
            .apply()

        AppCompatDelegate.setDefaultNightMode(
            if (modoOscuro) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
