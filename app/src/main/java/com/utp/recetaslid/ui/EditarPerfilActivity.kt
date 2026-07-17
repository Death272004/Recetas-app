package com.utp.recetaslid.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.data.SessionManager
import com.utp.recetaslid.databinding.ActivityEditarPerfilBinding

class EditarPerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarPerfilBinding
    private lateinit var db: DBHelper
    private lateinit var sesion: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)
        sesion = SessionManager(this)

        val userId = sesion.getUsuarioId()
        val usuario = db.obtenerUsuario(userId)

        binding.edtNombre.setText(usuario?.nombre ?: "")
        binding.txtCorreo.text = usuario?.correo ?: ""

        binding.btnVolver.setOnClickListener { finish() }

        binding.btnGuardar.setOnClickListener {
            val nuevoNombre = binding.edtNombre.text.toString().trim()
            if (nuevoNombre.isEmpty()) {
                Toast.makeText(this, "El nombre no puede estar vacio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            db.actualizarNombre(userId, nuevoNombre)
            sesion.actualizarNombre(nuevoNombre)
            Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnCambiarClave.setOnClickListener { cambiarClave(userId) }
    }

    // Cambia la contrasena validando primero la actual (RNF-03)
    private fun cambiarClave(userId: Int) {
        val actual = binding.edtClaveActual.text.toString()
        val nueva = binding.edtClaveNueva.text.toString()
        val confirmar = binding.edtClaveConfirmar.text.toString()

        if (actual.isEmpty() || nueva.isEmpty() || confirmar.isEmpty()) {
            Toast.makeText(this, "Completa los tres campos de contrasena", Toast.LENGTH_SHORT).show()
            return
        }
        if (nueva.length < 6) {
            Toast.makeText(this, "La nueva contrasena debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }
        if (nueva != confirmar) {
            Toast.makeText(this, "Las contrasenas nuevas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        val cambiada = db.cambiarClave(userId, actual, nueva)
        if (!cambiada) {
            Toast.makeText(this, "La contrasena actual no es correcta", Toast.LENGTH_SHORT).show()
            return
        }

        // Limpiamos los campos para no dejar las claves en pantalla
        binding.edtClaveActual.setText("")
        binding.edtClaveNueva.setText("")
        binding.edtClaveConfirmar.setText("")
        Toast.makeText(this, "Contrasena actualizada", Toast.LENGTH_SHORT).show()
    }
}
