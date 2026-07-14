package com.utp.recetaslid.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.utp.recetaslid.R
import com.utp.recetaslid.databinding.ItemFeedPostBinding
import com.utp.recetaslid.model.FeedPost

class FeedAdapter(
    private var posts: List<FeedPost>,
    private val alTocar: (FeedPost) -> Unit,
    private val alLike: (FeedPost) -> Unit,
    private val alComentar: (FeedPost) -> Unit,
    private val alCompartir: (FeedPost) -> Unit,
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
        holder.binding.txtUserName.text = post.userName
        holder.binding.txtTimeAgo.text = "Publicado"
        holder.binding.txtRecipeTitle.text = post.recipeTitle

        val ctx = holder.itemView.context
        val resId = if (post.imagen.isNotEmpty())
            ctx.resources.getIdentifier(post.imagen, "drawable", ctx.packageName)
        else 0

        if (resId != 0) {
            holder.binding.imgPost.setImageResource(resId)
            holder.binding.imgPost.scaleType = ImageView.ScaleType.CENTER_CROP
            holder.binding.imgPost.setPadding(0, 0, 0, 0)
            holder.binding.imgPost.setBackgroundColor(Color.TRANSPARENT)
        } else {
            val pad = (40 * ctx.resources.displayMetrics.density).toInt()
            holder.binding.imgPost.setImageResource(R.drawable.ic_receta)
            holder.binding.imgPost.scaleType = ImageView.ScaleType.CENTER_INSIDE
            holder.binding.imgPost.setPadding(pad, pad, pad, pad)
            holder.binding.imgPost.setBackgroundResource(R.color.naranja_suave)
        }

        if (post.isLiked) {
            holder.binding.btnLike.text = "♥  ${post.likes}"
            holder.binding.btnLike.setTextColor(ctx.getColor(R.color.naranja))
        } else {
            holder.binding.btnLike.text = "♡  ${post.likes}"
            holder.binding.btnLike.setTextColor(ctx.getColor(R.color.gris))
        }

        val comentarioText = if (post.comentarios > 0) "💬  ${post.comentarios}" else "💬  Comentar"
        holder.binding.btnComentar.text = comentarioText

        holder.binding.root.setOnClickListener { alTocar(post) }
        holder.binding.btnLike.setOnClickListener { alLike(post) }
        holder.binding.btnComentar.setOnClickListener { alComentar(post) }
        holder.binding.btnCompartir.setOnClickListener { alCompartir(post) }
        holder.binding.txtUserName.setOnClickListener { alTocarUsuario(post) }
        holder.binding.txtAvatar.setOnClickListener { alTocarUsuario(post) }
    }

    override fun getItemCount(): Int = posts.size

    fun actualizar(nueva: List<FeedPost>) {
        posts = nueva
        notifyDataSetChanged()
    }
}
