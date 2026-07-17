package com.utp.recetaslid.ui

import android.view.View
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.data.SessionManager
import com.utp.recetaslid.databinding.ActivityEditarPerfilBinding
import com.utp.recetaslid.model.Usuario
import com.utp.recetaslid.util.ImagenUtil

class EditarPerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarPerfilBinding
    private lateinit var db: DBHelper
    private lateinit var sesion: SessionManager
    private var usuarioActual: Usuario? = null
    private val selectorFoto = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@registerForActivityResult
        val userId = sesion.getUsuarioId()
        val ruta = ImagenUtil.guardarFotoPerfil(this, uri, userId)
        if (ruta == null) {
            Toast.makeText(this, "No se pudo guardar la foto", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        db.actualizarFotoPerfil(userId, ruta)
        usuarioActual = db.obtenerUsuario(userId)
        mostrarFoto()
        Toast.makeText(this, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)
        sesion = SessionManager(this)

        val userId = sesion.getUsuarioId()
        usuarioActual = db.obtenerUsuario(userId)

        binding.imgFotoPerfil.clipToOutline = true
        binding.txtInicialPerfil.clipToOutline = true
        binding.edtNombre.setText(usuarioActual?.nombre ?: "")
        binding.txtCorreo.text = usuarioActual?.correo ?: ""
        mostrarFoto()

        binding.btnVolver.setOnClickListener { finish() }
        binding.btnSubirFoto.setOnClickListener { selectorFoto.launch("image/*") }
        binding.btnEliminarFoto.setOnClickListener {
            val foto = usuarioActual?.foto ?: ""
            if (foto.isEmpty()) {
                Toast.makeText(this, "No tienes foto de perfil", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            ImagenUtil.eliminarArchivoLocal(foto)
            db.actualizarFotoPerfil(userId, "")
            usuarioActual = db.obtenerUsuario(userId)
            mostrarFoto()
            Toast.makeText(this, "Foto eliminada", Toast.LENGTH_SHORT).show()
        }

        binding.btnGuardar.setOnClickListener {
            val nuevoNombre = binding.edtNombre.text.toString().trim()
            if (nuevoNombre.isEmpty()) {
                Toast.makeText(this, "El nombre no puede estar vacio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            db.actualizarNombre(userId, nuevoNombre)
            sesion.actualizarNombre(nuevoNombre)
            usuarioActual = db.obtenerUsuario(userId)
            mostrarFoto()
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

    private fun mostrarFoto() {
        val usuario = usuarioActual
        val foto = usuario?.foto ?: ""
        val nombre = usuario?.nombre ?: sesion.getNombre()
        val hayFoto = foto.isNotEmpty() && ImagenUtil.mostrar(binding.imgFotoPerfil, foto, redondeado = true)
        binding.imgFotoPerfil.visibility = if (hayFoto) View.VISIBLE else View.GONE
        binding.txtInicialPerfil.visibility = if (hayFoto) View.GONE else View.VISIBLE
        if (!hayFoto) binding.txtInicialPerfil.text = nombre.firstOrNull()?.uppercase() ?: "?"
        binding.btnEliminarFoto.isEnabled = foto.isNotEmpty()
    }
}
