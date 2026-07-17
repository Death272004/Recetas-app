package com.utp.recetaslid.ui

import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.databinding.ActivityRegistroBinding

// Pantalla de registro de un nuevo usuario
class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding
    private lateinit var db: DBHelper

    // Preguntas de seguridad disponibles para recuperar la contrasena
    private val preguntas = listOf(
        "¿Nombre de tu primera mascota?",
        "¿Cual es tu comida favorita?",
        "¿En que ciudad naciste?",
        "¿Nombre de tu mejor amigo de la infancia?",
        "¿Cual fue tu primera escuela?"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)

        // Llenamos el desplegable con las preguntas de seguridad
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, preguntas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPregunta.adapter = adapter

        binding.btnVolver.setOnClickListener { finish() }
        binding.btnRegistrar.setOnClickListener { registrar() }
        binding.txtIrLogin.setOnClickListener { finish() }
    }

    private fun registrar() {
        val nombre = binding.edtNombre.text.toString().trim()
        val correo = binding.edtCorreo.text.toString().trim()
        val clave = binding.edtClave.text.toString()
        val confirmar = binding.edtConfirmar.text.toString()
        val pregunta = binding.spinnerPregunta.selectedItem?.toString() ?: preguntas[0]
        val respuesta = binding.edtRespuesta.text.toString().trim()

        // Validaciones de los campos del formulario
        if (nombre.isEmpty() || correo.isEmpty() || clave.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Ingresa un correo valido", Toast.LENGTH_SHORT).show()
            return
        }
        if (clave.length < 6) {
            Toast.makeText(this, "La contrasena debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }
        if (clave != confirmar) {
            Toast.makeText(this, "Las contrasenas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }
        if (respuesta.isEmpty()) {
            Toast.makeText(this, "Responde la pregunta de seguridad", Toast.LENGTH_SHORT).show()
            return
        }

        val creado = db.registrarUsuario(nombre, correo, clave, pregunta, respuesta)
        if (!creado) {
            Toast.makeText(this, "Ese correo ya esta registrado", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Cuenta creada. Ahora inicia sesion", Toast.LENGTH_LONG).show()
        finish()
    }
}
