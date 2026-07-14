package com.utp.recetaslid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.utp.recetaslid.databinding.ItemComentarioBinding
import com.utp.recetaslid.model.Comentario

class ComentarioAdapter(
    private var lista: List<Comentario>,
    private val miUsuarioId: Int,
    private val alEliminar: (Comentario) -> Unit
) : RecyclerView.Adapter<ComentarioAdapter.ComentarioViewHolder>() {

    inner class ComentarioViewHolder(val binding: ItemComentarioBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComentarioViewHolder {
        val binding = ItemComentarioBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ComentarioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ComentarioViewHolder, position: Int) {
        val c = lista[position]
        holder.binding.txtAvatarComentario.text = c.nombreUsuario.first().uppercase()
        holder.binding.txtNombreComentario.text = c.nombreUsuario
        holder.binding.txtTextoComentario.text = c.texto

        val millis = c.fecha.toLongOrNull() ?: 0L
        holder.binding.txtFechaComentario.text = tiempoRelativo(millis)

        if (c.usuarioId == miUsuarioId) {
            holder.binding.btnEliminarComentario.visibility = android.view.View.VISIBLE
            holder.binding.btnEliminarComentario.setOnClickListener { alEliminar(c) }
        } else {
            holder.binding.btnEliminarComentario.visibility = android.view.View.GONE
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizar(nueva: List<Comentario>) {
        lista = nueva
        notifyDataSetChanged()
    }

    private fun tiempoRelativo(millis: Long): String {
        val diff = System.currentTimeMillis() - millis
        val mins = diff / 60000
        val horas = mins / 60
        val dias = horas / 24
        return when {
            mins < 1 -> "Justo ahora"
            mins < 60 -> "Hace ${mins}min"
            horas < 24 -> "Hace ${horas}h"
            dias < 7 -> "Hace ${dias}d"
            else -> "Hace ${dias / 7}sem"
        }
    }
}
