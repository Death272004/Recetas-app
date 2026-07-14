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
    }
}
