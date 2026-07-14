package com.utp.recetaslid.ui

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.databinding.ActivityRegistroBinding

// Pantalla de registro de un nuevo usuario
class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding
    private lateinit var db: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)

        binding.btnVolver.setOnClickListener { finish() }
        binding.btnRegistrar.setOnClickListener { registrar() }
        binding.txtIrLogin.setOnClickListener { finish() }
    }

    private fun registrar() {
        val nombre = binding.edtNombre.text.toString().trim()
        val correo = binding.edtCorreo.text.toString().trim()
        val clave = binding.edtClave.text.toString()
        val confirmar = binding.edtConfirmar.text.toString()

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

        val creado = db.registrarUsuario(nombre, correo, clave)
        if (!creado) {
            Toast.makeText(this, "Ese correo ya esta registrado", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Cuenta creada. Ahora inicia sesion", Toast.LENGTH_LONG).show()
        finish()
    }
}
