package com.utp.recetaslid.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.databinding.ActivityRecuperarClaveBinding

// Recuperacion de contrasena en tres pasos:
// 1) el usuario indica su correo
// 2) responde su pregunta de seguridad
// 3) define una contrasena nueva
class RecuperarClaveActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecuperarClaveBinding
    private lateinit var db: DBHelper

    private var correoValidado = ""
    private var usuarioId = -1
    private var intentos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecuperarClaveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)

        binding.btnVolver.setOnClickListener { finish() }
        binding.btnBuscarCuenta.setOnClickListener { buscarCuenta() }
        binding.btnVerificar.setOnClickListener { verificarRespuesta() }
        binding.btnGuardarClave.setOnClickListener { guardarClave() }
    }

    // PASO 1: comprobamos que el correo tenga una pregunta de seguridad registrada
    private fun buscarCuenta() {
        val correo = binding.edtCorreo.text.toString().trim()
        if (correo.isEmpty()) {
            Toast.makeText(this, "Escribe tu correo", Toast.LENGTH_SHORT).show()
            return
        }

        val pregunta = db.obtenerPreguntaPorCorreo(correo)
        if (pregunta == null) {
            Toast.makeText(
                this,
                "No hay una cuenta con pregunta de seguridad para ese correo",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        correoValidado = correo
        binding.txtPregunta.text = pregunta
        binding.panelPaso1.visibility = View.GONE
        binding.panelPaso2.visibility = View.VISIBLE
    }

    // PASO 2: validamos la respuesta de seguridad
    private fun verificarRespuesta() {
        val respuesta = binding.edtRespuesta.text.toString().trim()
        if (respuesta.isEmpty()) {
            Toast.makeText(this, "Escribe tu respuesta", Toast.LENGTH_SHORT).show()
            return
        }

        val id = db.verificarRespuesta(correoValidado, respuesta)
        if (id == -1) {
            intentos++
            // Limitamos los intentos para que no se pueda adivinar la respuesta
            if (intentos >= 3) {
                Toast.makeText(this, "Demasiados intentos fallidos", Toast.LENGTH_LONG).show()
                finish()
                return
            }
            Toast.makeText(
                this,
                "Respuesta incorrecta. Te quedan ${3 - intentos} intentos",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        usuarioId = id
        binding.panelPaso2.visibility = View.GONE
        binding.panelPaso3.visibility = View.VISIBLE
    }

    // PASO 3: guardamos la contrasena nueva (se almacena como hash)
    private fun guardarClave() {
        val nueva = binding.edtNuevaClave.text.toString()
        val confirmar = binding.edtConfirmarClave.text.toString()

        if (nueva.isEmpty() || confirmar.isEmpty()) {
            Toast.makeText(this, "Completa los dos campos", Toast.LENGTH_SHORT).show()
            return
        }
        if (nueva.length < 6) {
            Toast.makeText(this, "La contrasena debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }
        if (nueva != confirmar) {
            Toast.makeText(this, "Las contrasenas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        db.resetearClave(usuarioId, nueva)
        Toast.makeText(this, "Contrasena actualizada. Ya puedes iniciar sesion", Toast.LENGTH_LONG).show()
        finish()
    }
}
