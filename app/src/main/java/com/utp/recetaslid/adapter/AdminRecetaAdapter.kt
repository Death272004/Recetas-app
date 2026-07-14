package com.utp.recetaslid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.utp.recetaslid.databinding.ItemAdminRecetaBinding
import com.utp.recetaslid.model.Receta

class AdminRecetaAdapter(
    private var lista: List<Receta>,
    private val obtenerNombreAutor: (Int) -> String,
    private val onDestacar: (Receta) -> Unit,
    private val onOcultar: (Receta) -> Unit,
    private val onQuitarReporte: (Receta) -> Unit,
    private val onEliminar: (Receta) -> Unit
) : RecyclerView.Adapter<AdminRecetaAdapter.VH>() {

    inner class VH(val b: ItemAdminRecetaBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemAdminRecetaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = lista[position]
        val b = holder.b

        b.txtTitulo.text = r.titulo
        val autor = if (r.autorId == 0) "Sistema" else obtenerNombreAutor(r.autorId)
        b.txtAutor.text = "Por: $autor"

        val cant = r.listaIngredientes().size
        b.txtMeta.text = "$cant ingredientes · ${r.tiempo} min · B/. ${"%.2f".format(r.costo)}"

        b.txtBadgeReportada.visibility = if (r.reportada) View.VISIBLE else View.GONE
        b.txtBadgeOculta.visibility = if (r.oculta) View.VISIBLE else View.GONE
        b.txtBadgeDestacada.visibility = if (r.destacada) View.VISIBLE else View.GONE

        b.btnDestacar.text = if (r.destacada) "Quitar destacado" else "Destacar"
        b.btnOcultar.text = if (r.oculta) "Mostrar" else "Ocultar"
        b.btnQuitarReporte.visibility = if (r.reportada) View.VISIBLE else View.GONE

        b.btnDestacar.setOnClickListener { onDestacar(r) }
        b.btnOcultar.setOnClickListener { onOcultar(r) }
        b.btnQuitarReporte.setOnClickListener { onQuitarReporte(r) }
        b.btnEliminar.setOnClickListener { onEliminar(r) }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizar(nueva: List<Receta>) {
        lista = nueva
        notifyDataSetChanged()
    }
}
