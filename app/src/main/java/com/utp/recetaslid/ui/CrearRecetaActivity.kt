package com.utp.recetaslid.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.data.SessionManager
import com.utp.recetaslid.databinding.ActivityCrearRecetaBinding
import com.utp.recetaslid.model.Receta

class CrearRecetaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrearRecetaBinding
    private lateinit var db: DBHelper
    private lateinit var sesion: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearRecetaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)
        sesion = SessionManager(this)

        binding.btnVolver.setOnClickListener { finish() }
        binding.btnGuardar.setOnClickListener { guardar() }
    }

    private fun guardar() {
        val titulo = binding.edtTitulo.text.toString().trim()
        val ingredientes = binding.edtIngredientes.text.toString().trim()
        val tiempoTexto = binding.edtTiempo.text.toString().trim()
        val costoTexto = binding.edtCosto.text.toString().trim()
        val pasos = binding.edtPasos.text.toString().trim()
        val videoUrl = binding.edtVideoUrl.text.toString().trim()

        if (titulo.isEmpty() || ingredientes.isEmpty() || tiempoTexto.isEmpty()
            || costoTexto.isEmpty() || pasos.isEmpty()
        ) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val tiempo = tiempoTexto.toIntOrNull()
        val costo = costoTexto.toDoubleOrNull()
        if (tiempo == null || costo == null) {
            Toast.makeText(this, "Tiempo y costo deben ser numeros", Toast.LENGTH_SHORT).show()
            return
        }

        val receta = Receta(
            id = 0,
            titulo = titulo,
            ingredientes = ingredientes,
            pasos = pasos,
            tiempo = tiempo,
            costo = costo,
            autorId = sesion.getUsuarioId(),
            videoUrl = videoUrl
        )
        db.insertarReceta(receta)
        Toast.makeText(this, "Receta guardada", Toast.LENGTH_SHORT).show()
        finish()
    }
}
