package com.utp.recetaslid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.utp.recetaslid.R
import com.utp.recetaslid.databinding.ItemAdminComentarioBinding
import com.utp.recetaslid.model.Comentario
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminComentarioAdapter(
    private var lista: List<Comentario>,
    private val onAprobar: (Comentario) -> Unit,
    private val onOcultar: (Comentario) -> Unit,
    private val onEliminar: (Comentario) -> Unit
) : RecyclerView.Adapter<AdminComentarioAdapter.VH>() {

    inner class VH(val b: ItemAdminComentarioBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemAdminComentarioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = lista[position]
        val b = holder.b

        b.txtUsuario.text = c.nombreUsuario
        b.txtReceta.text = "En: ${c.tituloReceta}"
        b.txtTexto.text = c.texto
        b.txtFecha.text = formatearFecha(c.fecha)

        val ctx = holder.itemView.context
        when (c.estado) {
            "aprobado" -> {
                b.txtEstadoBadge.text = "Aprobado"
                b.txtEstadoBadge.setBackgroundResource(R.drawable.bg_badge_verde)
                b.txtEstadoBadge.setTextColor(ctx.getColor(R.color.verde))
            }
            "oculto" -> {
                b.txtEstadoBadge.text = "Oculto"
                b.txtEstadoBadge.setBackgroundResource(R.drawable.bg_badge_gris)
                b.txtEstadoBadge.setTextColor(ctx.getColor(R.color.gris))
            }
            "pendiente" -> {
                b.txtEstadoBadge.text = "Pendiente"
                b.txtEstadoBadge.setBackgroundResource(R.drawable.bg_badge_naranja)
                b.txtEstadoBadge.setTextColor(ctx.getColor(R.color.naranja))
            }
            else -> {
                b.txtEstadoBadge.text = c.estado.replaceFirstChar { it.uppercase() }
                b.txtEstadoBadge.setBackgroundResource(R.drawable.bg_badge_gris)
                b.txtEstadoBadge.setTextColor(ctx.getColor(R.color.gris))
            }
        }

        b.btnAprobar.setOnClickListener { onAprobar(c) }
        b.btnOcultar.setOnClickListener { onOcultar(c) }
        b.btnEliminar.setOnClickListener { onEliminar(c) }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizar(nueva: List<Comentario>) {
        lista = nueva
        notifyDataSetChanged()
    }

    private fun formatearFecha(millis: String): String {
        return try {
            val ms = millis.toLong()
            val diff = System.currentTimeMillis() - ms
            when {
                diff < 60_000 -> "Hace un momento"
                diff < 3_600_000 -> "Hace ${diff / 60_000} min"
                diff < 86_400_000 -> "Hace ${diff / 3_600_000} horas"
                else -> {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    sdf.format(Date(ms))
                }
            }
        } catch (_: Exception) {
            millis
        }
    }
}
