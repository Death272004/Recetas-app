package com.utp.recetaslid.ui

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.utp.recetaslid.databinding.ActivityNotificacionesBinding

class NotificacionesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificacionesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificacionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("notificaciones_recetas", Context.MODE_PRIVATE)

        binding.switchNuevasRecetas.isChecked = prefs.getBoolean("nuevas_recetas", true)
        binding.switchLikes.isChecked = prefs.getBoolean("likes", true)
        binding.switchComentarios.isChecked = prefs.getBoolean("comentarios", true)
        binding.switchOfertas.isChecked = prefs.getBoolean("ofertas", false)

        val guardar = {
            prefs.edit()
                .putBoolean("nuevas_recetas", binding.switchNuevasRecetas.isChecked)
                .putBoolean("likes", binding.switchLikes.isChecked)
                .putBoolean("comentarios", binding.switchComentarios.isChecked)
                .putBoolean("ofertas", binding.switchOfertas.isChecked)
                .apply()
            Toast.makeText(this, "Preferencias guardadas", Toast.LENGTH_SHORT).show()
        }

        binding.switchNuevasRecetas.setOnCheckedChangeListener { _, _ -> guardar() }
        binding.switchLikes.setOnCheckedChangeListener { _, _ -> guardar() }
        binding.switchComentarios.setOnCheckedChangeListener { _, _ -> guardar() }
        binding.switchOfertas.setOnCheckedChangeListener { _, _ -> guardar() }

        binding.btnVolver.setOnClickListener { finish() }
    }
}
