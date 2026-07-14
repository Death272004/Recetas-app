package com.utp.recetaslid.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.utp.recetaslid.adapter.AdminUsuarioAdapter
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.data.SessionManager
import com.utp.recetaslid.databinding.ActivityAdminUsuariosBinding
import com.utp.recetaslid.model.Usuario

class AdminUsuariosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminUsuariosBinding
    private lateinit var db: DBHelper
    private lateinit var sesion: SessionManager
    private lateinit var adapter: AdminUsuarioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminUsuariosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)
        sesion = SessionManager(this)

        adapter = AdminUsuarioAdapter(
            emptyList(),
            onCambiarRol = { cambiarRol(it) },
            onCambiarEstado = { cambiarEstado(it) },
            onResetClave = { resetearClave(it) },
            onEliminar = { eliminarUsuario(it) }
        )
        binding.recyclerUsuarios.layoutManager = LinearLayoutManager(this)
        binding.recyclerUsuarios.adapter = adapter

        binding.btnVolver.setOnClickListener { finish() }

        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { refrescar() }
        })
    }

    override fun onResume() {
        super.onResume()
        refrescar()
    }

    private fun refrescar() {
        val query = binding.etBuscar.text.toString().trim()
        val lista = if (query.isEmpty()) db.listarUsuariosAdmin()
        else db.buscarUsuariosAdmin(query)

        adapter.actualizar(lista)
        binding.txtConteo.text = "${lista.size} usuarios"
        binding.txtVacio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun cambiarRol(u: Usuario) {
        val nuevoRol = if (u.rol == "usuario") "admin" else "usuario"
        AlertDialog.Builder(this)
            .setTitle("Cambiar rol")
            .setMessage("Cambiar rol de \"${u.nombre}\" a $nuevoRol?")
            .setPositiveButton("Confirmar") { _, _ ->
                db.cambiarRolUsuario(u.id, nuevoRol)
                db.registrarLog(sesion.getUsuarioId(), "Cambio de rol", "Usuario \"${u.nombre}\" cambiado a $nuevoRol")
                Toast.makeText(this, "Rol actualizado", Toast.LENGTH_SHORT).show()
                refrescar()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cambiarEstado(u: Usuario) {
        val opciones = arrayOf("Activo", "Suspendido", "Bloqueado")
        val valores = arrayOf("activo", "suspendido", "bloqueado")
        AlertDialog.Builder(this)
            .setTitle("Cambiar estado de ${u.nombre}")
            .setItems(opciones) { _, which ->
                val nuevoEstado = valores[which]
                db.cambiarEstadoUsuario(u.id, nuevoEstado)
                db.registrarLog(sesion.getUsuarioId(), "Cambio de estado", "Usuario \"${u.nombre}\" cambiado a $nuevoEstado")
                Toast.makeText(this, "Estado actualizado a $nuevoEstado", Toast.LENGTH_SHORT).show()
                refrescar()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun resetearClave(u: Usuario) {
        AlertDialog.Builder(this)
            .setTitle("Resetear contrasena")
            .setMessage("La contrasena de \"${u.nombre}\" sera cambiada a \"123456\". Continuar?")
            .setPositiveButton("Resetear") { _, _ ->
                db.resetearClave(u.id, "123456")
                db.registrarLog(sesion.getUsuarioId(), "Reset de contrasena", "Contrasena de \"${u.nombre}\" reseteada")
                Toast.makeText(this, "Contrasena reseteada a 123456", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarUsuario(u: Usuario) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar usuario")
            .setMessage("Seguro que quieres eliminar a \"${u.nombre}\"? Esta accion no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                db.moverAPapelera("usuario", "ID:${u.id} Nombre:${u.nombre} Correo:${u.correo}", sesion.getUsuarioId())
                db.eliminarUsuario(u.id)
                db.registrarLog(sesion.getUsuarioId(), "Eliminacion de usuario", "Usuario \"${u.nombre}\" eliminado")
                Toast.makeText(this, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                refrescar()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
