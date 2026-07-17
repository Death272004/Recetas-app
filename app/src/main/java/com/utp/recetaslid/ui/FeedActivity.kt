package com.utp.recetaslid.ui

import android.content.Intent
import android.os.Bundle
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
                // El invitado debe iniciar sesion para abrir el detalle
                if (!sesion.haySesion()) {
                    LoginRapido.mostrar(this, "Entra a tu cuenta para ver esta receta") {
                        esInvitado = false
                        verDetalle(post.recetaId)
                    }
                } else {
                    verDetalle(post.recetaId)
                }
            },
            alLike = { post ->
                if (!sesion.haySesion()) {
                    LoginRapido.mostrar(this, "Entra a tu cuenta para dar like") {
                        esInvitado = false
                        db.alternarFavorito(sesion.getUsuarioId(), post.recetaId)
                        cargarFeed()
                    }
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
            alVerLikes = { post ->
                val i = Intent(this, LikesUsuariosActivity::class.java)
                i.putExtra("recetaId", post.recetaId)
                i.putExtra("recetaTitulo", post.recipeTitle)
                startActivity(i)
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

    private fun verDetalle(recetaId: Int) {
        val i = Intent(this, DetalleRecetaActivity::class.java)
        i.putExtra("recetaId", recetaId)
        startActivity(i)
    }

    override fun onResume() {
        super.onResume()
        esInvitado = !sesion.haySesion()
        cargarFeed()
    }

    private fun cargarFeed() {
        val recetas = db.listarRecetas()
        val userId = sesion.getUsuarioId()
        val posts = recetas.map { r ->
            val usuarioAutor = if (r.autorId == 0) null else db.obtenerUsuario(r.autorId)
            val autor = if (r.autorId == 0) "RECETAS LID" else usuarioAutor?.nombre ?: "Usuario"
            FeedPost(
                recetaId = r.id,
                autorId = r.autorId,
                userName = autor,
                userInitial = autor.firstOrNull()?.uppercase() ?: "?",
                userPhoto = usuarioAutor?.foto ?: "",
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
