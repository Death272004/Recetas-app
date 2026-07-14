package com.utp.recetaslid.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.utp.recetaslid.adapter.ComentarioAdapter
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.data.SessionManager
import com.utp.recetaslid.databinding.ActivityComentariosBinding

class ComentariosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityComentariosBinding
    private lateinit var db: DBHelper
    private lateinit var sesion: SessionManager
    private lateinit var adapter: ComentarioAdapter
    private var recetaId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComentariosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)
        sesion = SessionManager(this)
        recetaId = intent.getIntExtra("recetaId", -1)
        val recetaTitulo = intent.getStringExtra("recetaTitulo") ?: "Comentarios"

        binding.txtTituloComentarios.text = recetaTitulo

        val userId = sesion.getUsuarioId()

        adapter = ComentarioAdapter(emptyList(), userId) { comentario ->
            db.eliminarComentario(comentario.id)
            cargarComentarios()
        }
        binding.recyclerComentarios.layoutManager = LinearLayoutManager(this)
        binding.recyclerComentarios.adapter = adapter

        binding.btnVolver.setOnClickListener { finish() }

        if (!sesion.haySesion()) {
            binding.layoutEscribir.visibility = View.GONE
        }

        binding.btnEnviar.setOnClickListener {
            val texto = binding.edtComentario.text.toString().trim()
            if (texto.isEmpty()) {
                Toast.makeText(this, "Escribe un comentario", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            db.insertarComentario(userId, recetaId, texto)
            binding.edtComentario.text.clear()
            cargarComentarios()
        }
    }

    override fun onResume() {
        super.onResume()
        cargarComentarios()
    }

    private fun cargarComentarios() {
        val comentarios = db.listarComentarios(recetaId)
        adapter.actualizar(comentarios)
        binding.txtSinComentarios.visibility = if (comentarios.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerComentarios.visibility = if (comentarios.isEmpty()) View.GONE else View.VISIBLE
    }
}
