package com.utp.recetaslid.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.utp.recetaslid.adapter.LikeUsuarioAdapter
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.databinding.ActivityLikesUsuariosBinding

class LikesUsuariosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLikesUsuariosBinding
    private lateinit var db: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLikesUsuariosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)
        val recetaId = intent.getIntExtra("recetaId", -1)
        val titulo = intent.getStringExtra("recetaTitulo") ?: "Receta"
        binding.txtTitulo.text = "Likes"
        binding.txtSubtitulo.text = titulo
        binding.btnVolver.setOnClickListener { finish() }

        val usuarios = db.listarUsuariosQueDieronLike(recetaId)
        binding.txtVacio.visibility = if (usuarios.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerLikes.visibility = if (usuarios.isEmpty()) View.GONE else View.VISIBLE
        binding.recyclerLikes.layoutManager = LinearLayoutManager(this)
        binding.recyclerLikes.adapter = LikeUsuarioAdapter(usuarios) { usuario ->
            val i = Intent(this, PerfilPublicoActivity::class.java)
            i.putExtra("usuarioId", usuario.id)
            startActivity(i)
        }
    }
}
