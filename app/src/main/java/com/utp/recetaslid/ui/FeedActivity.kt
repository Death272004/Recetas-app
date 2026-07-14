package com.utp.recetaslid.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.utp.recetaslid.adapter.FeedAdapter
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.data.SessionManager
import com.utp.recetaslid.databinding.ActivityFeedBinding
import com.utp.recetaslid.model.FeedPost

class FeedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedBinding
    private lateinit var db: DBHelper
    private lateinit var sesion: SessionManager
    private lateinit var adapter: FeedAdapter
    private var esInvitado = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)
        sesion = SessionManager(this)
        esInvitado = !sesion.haySesion()

        binding.btnVolver.setOnClickListener { finish() }

        adapter = FeedAdapter(
            emptyList(),
            alTocar = { post ->
                val i = Intent(this, DetalleRecetaActivity::class.java)
                i.putExtra("recetaId", post.recetaId)
                startActivity(i)
            },
            alLike = { post ->
                if (esInvitado) {
                    Toast.makeText(this, "Inicia sesion para dar like", Toast.LENGTH_SHORT).show()
                    return@FeedAdapter
                }
                db.alternarFavorito(sesion.getUsuarioId(), post.recetaId)
                cargarFeed()
            },
            alComentar = { post ->
                val i = Intent(this, ComentariosActivity::class.java)
                i.putExtra("recetaId", post.recetaId)
                i.putExtra("recetaTitulo", post.recipeTitle)
                startActivity(i)
            },
            alCompartir = { post ->
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Mira esta receta: ${post.recipeTitle}\nDescargala en RECETAS LID")
                }
                startActivity(Intent.createChooser(shareIntent, "Compartir receta"))
            },
            alTocarUsuario = { post ->
                if (post.autorId != 0) {
                    val i = Intent(this, PerfilPublicoActivity::class.java)
                    i.putExtra("usuarioId", post.autorId)
                    startActivity(i)
                }
            }
        )
        binding.recyclerFeed.layoutManager = LinearLayoutManager(this)
        binding.recyclerFeed.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        cargarFeed()
    }

    private fun cargarFeed() {
        val recetas = db.listarRecetas()
        val userId = sesion.getUsuarioId()
        val posts = recetas.map { r ->
            val autor = if (r.autorId == 0) "RECETAS LID"
            else db.obtenerUsuario(r.autorId)?.nombre ?: "Usuario"
            FeedPost(
                recetaId = r.id,
                autorId = r.autorId,
                userName = autor,
                userInitial = autor.first().uppercase(),
                recipeTitle = r.titulo,
                imagen = r.imagen,
                likes = db.contarLikesReceta(r.id),
                isLiked = if (userId != -1) db.esFavorito(userId, r.id) else false,
                comentarios = db.contarComentarios(r.id)
            )
        }
        adapter.actualizar(posts)
    }
}
