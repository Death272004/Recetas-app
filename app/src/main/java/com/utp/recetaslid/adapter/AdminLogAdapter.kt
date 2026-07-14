package com.utp.recetaslid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.utp.recetaslid.databinding.ItemAdminLogBinding
import com.utp.recetaslid.model.LogAdmin
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminLogAdapter(
    private var lista: List<LogAdmin>
) : RecyclerView.Adapter<AdminLogAdapter.VH>() {

    inner class VH(val b: ItemAdminLogBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemAdminLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val log = lista[position]
        val b = holder.b

        b.txtAccion.text = log.accion
        b.txtDetalle.text = log.detalle
        b.txtAdmin.text = "Por: ${log.nombreAdmin}"
        b.txtFecha.text = " · ${formatearFecha(log.fecha)}"
    }

    override fun getItemCount(): Int = lista.size

    fun actualizar(nueva: List<LogAdmin>) {
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
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    sdf.format(Date(ms))
                }
            }
        } catch (_: Exception) {
            millis
        }
    }
}
