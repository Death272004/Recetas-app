package com.utp.recetaslid.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.utp.recetaslid.adapter.RecetaAdapter
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.databinding.ActivityPerfilPublicoBinding
import com.utp.recetaslid.util.ImagenUtil

class PerfilPublicoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilPublicoBinding
    private lateinit var db: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilPublicoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)
        val usuarioId = intent.getIntExtra("usuarioId", -1)
        val usuario = db.obtenerUsuario(usuarioId)

        if (usuario == null) {
            finish()
            return
        }

        binding.txtTituloPerfil.text = usuario.nombre
        binding.txtNombrePublico.text = usuario.nombre
        binding.imgAvatarGrande.clipToOutline = true
        binding.txtAvatarGrande.clipToOutline = true
        val hayFoto = usuario.foto.isNotEmpty() &&
            ImagenUtil.mostrar(binding.imgAvatarGrande, usuario.foto, redondeado = true)
        binding.imgAvatarGrande.visibility = if (hayFoto) View.VISIBLE else View.GONE
        binding.txtAvatarGrande.visibility = if (hayFoto) View.GONE else View.VISIBLE
        if (!hayFoto) binding.txtAvatarGrande.text = usuario.nombre.firstOrNull()?.uppercase() ?: "?"

        val recetas = db.listarRecetasDeUsuario(usuarioId)
        binding.txtRecetasPublico.text = recetas.size.toString()

        var totalLikes = 0
        for (r in recetas) {
            totalLikes += db.contarLikesReceta(r.id)
        }
        binding.txtLikesPublico.text = totalLikes.toString()

        val adapter = RecetaAdapter(recetas) { receta ->
            val i = Intent(this, DetalleRecetaActivity::class.java)
            i.putExtra("recetaId", receta.id)
            startActivity(i)
        }
        binding.recyclerRecetasPublico.layoutManager = LinearLayoutManager(this)
        binding.recyclerRecetasPublico.adapter = adapter

        binding.txtSinRecetas.visibility = if (recetas.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerRecetasPublico.visibility = if (recetas.isEmpty()) View.GONE else View.VISIBLE

        binding.btnVolver.setOnClickListener { finish() }
    }
}
