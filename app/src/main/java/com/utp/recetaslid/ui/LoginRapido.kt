package com.utp.recetaslid.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.data.SessionManager
import com.utp.recetaslid.databinding.DialogLoginBinding

// Mini pantalla de inicio de sesion que aparece encima de la pantalla actual.
// Se usa cuando un invitado intenta entrar a una funcion que requiere cuenta:
// asi no pierde lo que estaba viendo y, al entrar, continua donde iba.
object LoginRapido {

    // Muestra el dialogo. Si el login es correcto, ejecuta alEntrar().
    fun mostrar(activity: AppCompatActivity, mensaje: String, alEntrar: () -> Unit) {
        val binding = DialogLoginBinding.inflate(activity.layoutInflater)
        val db = DBHelper(activity)
        val sesion = SessionManager(activity)

        binding.txtMensaje.text = mensaje

        val dialogo = AlertDialog.Builder(activity)
            .setView(binding.root)
            .create()

        // Fondo transparente para que se vean las esquinas redondeadas del layout
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.btnEntrar.setOnClickListener {
            val correo = binding.edtCorreo.text.toString().trim()
            val clave = binding.edtClave.text.toString()

            if (correo.isEmpty() || clave.isEmpty()) {
                Toast.makeText(activity, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val usuario = db.login(correo, clave)
            if (usuario == null) {
                // Reutilizamos los mismos mensajes de la pantalla de login normal
                val estado = db.obtenerEstadoPorCredenciales(correo, clave)
                val msg = when (estado) {
                    "bloqueado" -> "Tu cuenta ha sido bloqueada. Contacta al administrador."
                    "suspendido" -> "Tu cuenta esta suspendida temporalmente."
                    else -> "Correo o contrasena incorrectos"
                }
                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // El administrador tiene su propio panel, no la vista de recetas
            if (usuario.rol == "admin") {
                sesion.guardarSesion(usuario.id, usuario.nombre, usuario.rol)
                dialogo.dismiss()
                val i = Intent(activity, AdminActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                activity.startActivity(i)
                return@setOnClickListener
            }

            sesion.guardarSesion(usuario.id, usuario.nombre, usuario.rol)
            Toast.makeText(activity, "Bienvenido, ${usuario.nombre}", Toast.LENGTH_SHORT).show()
            dialogo.dismiss()
            // Continuamos con la accion que el invitado queria hacer
            alEntrar()
        }

        binding.txtCrearCuenta.setOnClickListener {
            dialogo.dismiss()
            activity.startActivity(Intent(activity, RegistroActivity::class.java))
        }

        binding.txtOlvide.setOnClickListener {
            dialogo.dismiss()
            activity.startActivity(Intent(activity, RecuperarClaveActivity::class.java))
        }

        dialogo.show()
    }
}
