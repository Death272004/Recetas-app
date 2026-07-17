package com.utp.recetaslid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.utp.recetaslid.R
import com.utp.recetaslid.databinding.ItemFeedPostBinding
import com.utp.recetaslid.model.FeedPost
import com.utp.recetaslid.util.ImagenUtil

class FeedAdapter(
    private var posts: List<FeedPost>,
    private val alTocar: (FeedPost) -> Unit,
    private val alLike: (FeedPost) -> Unit,
    private val alComentar: (FeedPost) -> Unit,
    private val alCompartir: (FeedPost) -> Unit,
    private val alVerLikes: (FeedPost) -> Unit,
    private val alTocarUsuario: (FeedPost) -> Unit
) : RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {

    inner class FeedViewHolder(val binding: ItemFeedPostBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val binding = ItemFeedPostBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FeedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val post = posts[position]
        holder.binding.txtAvatar.text = post.userInitial
        holder.binding.imgAvatar.clipToOutline = true
        val hayFotoPerfil = ImagenUtil.mostrar(holder.binding.imgAvatar, post.userPhoto, redondeado = true)
        holder.binding.imgAvatar.visibility = if (hayFotoPerfil) View.VISIBLE else View.GONE
        holder.binding.txtAvatar.visibility = if (hayFotoPerfil) View.GONE else View.VISIBLE
        holder.binding.txtUserName.text = post.userName
        holder.binding.txtTimeAgo.text = "Publicado"
        holder.binding.txtRecipeTitle.text = post.recipeTitle

        val ctx = holder.itemView.context
        val hayFoto = ImagenUtil.mostrar(holder.binding.imgPost, post.imagen, paddingVacioDp = 40)
        holder.binding.imgPost.scaleType =
            if (hayFoto) ImageView.ScaleType.CENTER_CROP else ImageView.ScaleType.CENTER_INSIDE

        if (post.isLiked) {
            holder.binding.btnLike.text = "♥"
            holder.binding.btnLike.setTextColor(ctx.getColor(R.color.naranja))
        } else {
            holder.binding.btnLike.text = "♡"
            holder.binding.btnLike.setTextColor(ctx.getColor(R.color.gris))
        }
        holder.binding.txtLikes.text = if (post.likes == 1) "1 Me gusta" else "${post.likes} Me gusta"

        val comentarioText = if (post.comentarios > 0) "💬  ${post.comentarios}" else "💬  Comentar"
        holder.binding.btnComentar.text = comentarioText

        holder.binding.root.setOnClickListener { alTocar(post) }
        holder.binding.btnLike.setOnClickListener { alLike(post) }
        holder.binding.btnComentar.setOnClickListener { alComentar(post) }
        holder.binding.btnCompartir.setOnClickListener { alCompartir(post) }
        holder.binding.txtLikes.setOnClickListener { alVerLikes(post) }
        holder.binding.txtUserName.setOnClickListener { alTocarUsuario(post) }
        holder.binding.txtAvatar.setOnClickListener { alTocarUsuario(post) }
        holder.binding.imgAvatar.setOnClickListener { alTocarUsuario(post) }
    }

    override fun getItemCount(): Int = posts.size

    fun actualizar(nueva: List<FeedPost>) {
        posts = nueva
        notifyDataSetChanged()
    }
}
