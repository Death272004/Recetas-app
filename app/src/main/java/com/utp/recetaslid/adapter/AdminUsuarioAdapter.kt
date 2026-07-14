package com.utp.recetaslid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.utp.recetaslid.R
import com.utp.recetaslid.databinding.ItemAdminUsuarioBinding
import com.utp.recetaslid.model.Usuario
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminUsuarioAdapter(
    private var lista: List<Usuario>,
    private val onCambiarRol: (Usuario) -> Unit,
    private val onCambiarEstado: (Usuario) -> Unit,
    private val onResetClave: (Usuario) -> Unit,
    private val onEliminar: (Usuario) -> Unit
) : RecyclerView.Adapter<AdminUsuarioAdapter.VH>() {

    inner class VH(val b: ItemAdminUsuarioBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemAdminUsuarioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val u = lista[position]
        val b = holder.b

        b.txtAvatar.text = u.nombre.firstOrNull()?.uppercase() ?: "?"
        b.txtNombre.text = u.nombre
        b.txtCorreo.text = u.correo

        if (u.rol == "admin") {
            b.txtRolBadge.text = "Admin"
            b.txtRolBadge.setBackgroundResource(R.drawable.bg_badge_naranja)
            b.txtRolBadge.setTextColor(holder.itemView.context.getColor(R.color.naranja))
        } else {
            b.txtRolBadge.text = "Usuario"
            b.txtRolBadge.setBackgroundResource(R.drawable.bg_badge_azul)
            b.txtRolBadge.setTextColor(holder.itemView.context.getColor(R.color.azul))
        }

        when (u.estado) {
            "activo" -> {
                b.txtEstadoBadge.text = "Activo"
                b.txtEstadoBadge.setBackgroundResource(R.drawable.bg_badge_verde)
                b.txtEstadoBadge.setTextColor(holder.itemView.context.getColor(R.color.verde))
            }
            "suspendido" -> {
                b.txtEstadoBadge.text = "Suspendido"
                b.txtEstadoBadge.setBackgroundResource(R.drawable.bg_badge_naranja)
                b.txtEstadoBadge.setTextColor(holder.itemView.context.getColor(R.color.naranja))
            }
            "bloqueado" -> {
                b.txtEstadoBadge.text = "Bloqueado"
                b.txtEstadoBadge.setBackgroundResource(R.drawable.bg_badge_rojo)
                b.txtEstadoBadge.setTextColor(holder.itemView.context.getColor(R.color.rojo))
            }
            else -> {
                b.txtEstadoBadge.text = "Activo"
                b.txtEstadoBadge.setBackgroundResource(R.drawable.bg_badge_verde)
                b.txtEstadoBadge.setTextColor(holder.itemView.context.getColor(R.color.verde))
            }
        }

        val registro = formatearFecha(u.fechaRegistro)
        val acceso = formatearFecha(u.ultimoAcceso)
        b.txtFechas.text = "Registro: $registro  |  Ultimo acceso: $acceso"

        val esAdmin = u.rol == "admin"
        b.btnCambiarRol.visibility = if (esAdmin) View.GONE else View.VISIBLE
        b.btnCambiarEstado.visibility = if (esAdmin) View.GONE else View.VISIBLE
        b.btnResetClave.visibility = if (esAdmin) View.GONE else View.VISIBLE
        b.btnEliminar.visibility = if (esAdmin) View.GONE else View.VISIBLE

        b.btnCambiarRol.setOnClickListener { onCambiarRol(u) }
        b.btnCambiarEstado.setOnClickListener { onCambiarEstado(u) }
        b.btnResetClave.setOnClickListener { onResetClave(u) }
        b.btnEliminar.setOnClickListener { onEliminar(u) }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizar(nueva: List<Usuario>) {
        lista = nueva
        notifyDataSetChanged()
    }

    private fun formatearFecha(millis: String): String {
        if (millis.isEmpty()) return "--"
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(Date(millis.toLong()))
        } catch (_: Exception) {
            "--"
        }
    }
}
