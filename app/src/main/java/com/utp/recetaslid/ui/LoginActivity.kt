package com.utp.recetaslid.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.data.SessionManager
import com.utp.recetaslid.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var db: DBHelper
    private lateinit var sesion: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)
        sesion = SessionManager(this)

        binding.btnVolver.setOnClickListener { finish() }
        binding.btnEntrar.setOnClickListener { iniciarSesion() }
        binding.txtIrRegistro.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
        binding.txtInvitado.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun iniciarSesion() {
        val correo = binding.edtCorreo.text.toString().trim()
        val clave = binding.edtClave.text.toString()

        if (correo.isEmpty() || clave.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val usuario = db.login(correo, clave)
        if (usuario == null) {
            Toast.makeText(this, "Correo o contrasena incorrectos", Toast.LENGTH_SHORT).show()
            return
        }

        sesion.guardarSesion(usuario.id, usuario.nombre, usuario.rol)
        Toast.makeText(this, "Bienvenido, ${usuario.nombre}", Toast.LENGTH_SHORT).show()

        val destino = if (usuario.rol == "admin") AdminActivity::class.java
        else MainActivity::class.java
        val intent = Intent(this, destino)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
