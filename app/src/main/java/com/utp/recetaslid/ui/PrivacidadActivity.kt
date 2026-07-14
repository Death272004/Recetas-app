package com.utp.recetaslid.ui

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.utp.recetaslid.databinding.ActivityPrivacidadBinding

class PrivacidadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacidadBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacidadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("privacidad_recetas", Context.MODE_PRIVATE)

        binding.switchPerfilPublico.isChecked = prefs.getBoolean("perfil_publico", true)
        binding.switchMostrarFavoritos.isChecked = prefs.getBoolean("mostrar_favoritos", false)

        val guardar = {
            prefs.edit()
                .putBoolean("perfil_publico", binding.switchPerfilPublico.isChecked)
                .putBoolean("mostrar_favoritos", binding.switchMostrarFavoritos.isChecked)
                .apply()
            Toast.makeText(this, "Preferencias guardadas", Toast.LENGTH_SHORT).show()
        }

        binding.switchPerfilPublico.setOnCheckedChangeListener { _, _ -> guardar() }
        binding.switchMostrarFavoritos.setOnCheckedChangeListener { _, _ -> guardar() }

        binding.btnEliminarCuenta.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Eliminar cuenta")
                .setMessage("Para eliminar tu cuenta, contacta al administrador de la aplicacion. Todos tus datos seran eliminados permanentemente.")
                .setPositiveButton("Entendido", null)
                .show()
        }

        binding.btnVolver.setOnClickListener { finish() }
    }
}
