package com.utp.recetaslid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.utp.recetaslid.databinding.ItemLikeUsuarioBinding
import com.utp.recetaslid.model.Usuario
import com.utp.recetaslid.util.ImagenUtil

class LikeUsuarioAdapter(
    private var usuarios: List<Usuario>,
    private val alTocar: (Usuario) -> Unit
) : RecyclerView.Adapter<LikeUsuarioAdapter.LikeUsuarioViewHolder>() {

    inner class LikeUsuarioViewHolder(val binding: ItemLikeUsuarioBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeUsuarioViewHolder {
        val binding = ItemLikeUsuarioBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LikeUsuarioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LikeUsuarioViewHolder, position: Int) {
        val usuario = usuarios[position]
        holder.binding.txtNombre.text = usuario.nombre
        holder.binding.txtCorreo.text = usuario.correo
        holder.binding.imgAvatar.clipToOutline = true
        holder.binding.txtInicial.clipToOutline = true
        val hayFoto = usuario.foto.isNotEmpty() &&
            ImagenUtil.mostrar(holder.binding.imgAvatar, usuario.foto, redondeado = true)
        holder.binding.imgAvatar.visibility = if (hayFoto) View.VISIBLE else View.GONE
        holder.binding.txtInicial.visibility = if (hayFoto) View.GONE else View.VISIBLE
        if (!hayFoto) holder.binding.txtInicial.text = usuario.nombre.firstOrNull()?.uppercase() ?: "?"
        holder.binding.root.setOnClickListener { alTocar(usuario) }
    }

    override fun getItemCount(): Int = usuarios.size
}
