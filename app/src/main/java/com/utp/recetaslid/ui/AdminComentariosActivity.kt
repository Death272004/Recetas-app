package com.utp.recetaslid.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.utp.recetaslid.adapter.AdminComentarioAdapter
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.data.SessionManager
import com.utp.recetaslid.databinding.ActivityAdminComentariosBinding

class AdminComentariosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminComentariosBinding
    private lateinit var db: DBHelper
    private lateinit var sesion: SessionManager
    private lateinit var adapter: AdminComentarioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminComentariosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)
        sesion = SessionManager(this)

        adapter = AdminComentarioAdapter(
            emptyList(),
            onAprobar = { c ->
                db.cambiarEstadoComentario(c.id, "aprobado")
                db.registrarLog(sesion.getUsuarioId(), "Aprobar comentario", "Comentario #${c.id} de ${c.nombreUsuario}")
                Toast.makeText(this, "Comentario aprobado", Toast.LENGTH_SHORT).show()
                refrescar()
            },
            onOcultar = { c ->
                db.cambiarEstadoComentario(c.id, "oculto")
                db.registrarLog(sesion.getUsuarioId(), "Ocultar comentario", "Comentario #${c.id} de ${c.nombreUsuario}")
                Toast.makeText(this, "Comentario oculto", Toast.LENGTH_SHORT).show()
                refrescar()
            },
            onEliminar = { c ->
                AlertDialog.Builder(this)
                    .setTitle("Eliminar comentario")
                    .setMessage("Eliminar el comentario de ${c.nombreUsuario}?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        db.moverAPapelera("comentario", "ID:${c.id} Usuario:${c.nombreUsuario} Texto:${c.texto}", sesion.getUsuarioId())
                        db.eliminarComentario(c.id)
                        db.registrarLog(sesion.getUsuarioId(), "Eliminacion de comentario", "Comentario #${c.id} de ${c.nombreUsuario}")
                        Toast.makeText(this, "Comentario eliminado", Toast.LENGTH_SHORT).show()
                        refrescar()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )
        binding.recyclerComentarios.layoutManager = LinearLayoutManager(this)
        binding.recyclerComentarios.adapter = adapter

        binding.btnVolver.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        refrescar()
    }

    private fun refrescar() {
        val lista = db.listarTodosComentarios()
        adapter.actualizar(lista)
        binding.txtConteo.text = "${lista.size} comentarios"
        binding.txtVacio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
    }
}
