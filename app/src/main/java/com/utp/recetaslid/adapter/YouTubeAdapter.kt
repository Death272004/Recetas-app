package com.utp.recetaslid.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.utp.recetaslid.databinding.ItemYoutubeBinding
import com.utp.recetaslid.model.VideoYouTube

class YouTubeAdapter(
    private var lista: List<VideoYouTube>
) : RecyclerView.Adapter<YouTubeAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(val binding: ItemYoutubeBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemYoutubeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = lista[position]
        holder.binding.txtTituloVideo.text = video.titulo
        holder.binding.txtCanal.text = video.canal

        holder.binding.root.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.getUrl()))
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizar(nueva: List<VideoYouTube>) {
        lista = nueva
        notifyDataSetChanged()
    }
}
