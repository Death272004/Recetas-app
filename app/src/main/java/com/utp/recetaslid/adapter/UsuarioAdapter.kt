package com.utp.recetaslid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.utp.recetaslid.databinding.ItemUsuarioBinding
import com.utp.recetaslid.model.Usuario

// Adaptador para la lista de usuarios (vista del administrador)
class UsuarioAdapter(
    private var lista: List<Usuario>,
    private val alEliminar: (Usuario) -> Unit
) : RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>() {

    inner class UsuarioViewHolder(val binding: ItemUsuarioBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val binding = ItemUsuarioBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UsuarioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val u = lista[position]
        holder.binding.txtNombre.text = u.nombre
        holder.binding.txtRol.text = if (u.rol == "admin") "Administrador" else "Usuario"
        // El administrador no puede eliminarse a si mismo
        if (u.rol == "admin") {
            holder.binding.btnEliminar.visibility = android.view.View.GONE
        } else {
            holder.binding.btnEliminar.visibility = android.view.View.VISIBLE
            holder.binding.btnEliminar.setOnClickListener { alEliminar(u) }
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizar(nueva: List<Usuario>) {
        lista = nueva
        notifyDataSetChanged()
    }
}
