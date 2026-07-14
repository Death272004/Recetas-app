package com.utp.recetaslid.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.utp.recetaslid.R
import com.utp.recetaslid.databinding.ItemRecetaBinding
import com.utp.recetaslid.model.Receta

// Adaptador para mostrar la lista de recetas en un RecyclerView
class RecetaAdapter(
    private var lista: List<Receta>,
    private val alTocar: (Receta) -> Unit
) : RecyclerView.Adapter<RecetaAdapter.RecetaViewHolder>() {

    inner class RecetaViewHolder(val binding: ItemRecetaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecetaViewHolder {
        val binding = ItemRecetaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RecetaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecetaViewHolder, position: Int) {
        val receta = lista[position]
        holder.binding.txtTitulo.text = receta.titulo
        val cantidad = receta.listaIngredientes().size
        holder.binding.txtMeta.text = "$cantidad ingredientes \u00B7 ${receta.tiempo} min"
        holder.binding.txtCosto.text = "B/. ${"%.2f".format(receta.costo)}"
        holder.binding.root.setOnClickListener { alTocar(receta) }

        // Carga la imagen del plato si existe, o muestra el icono generico
        val ctx = holder.itemView.context
        val resId = if (receta.imagen.isNotEmpty())
            ctx.resources.getIdentifier(receta.imagen, "drawable", ctx.packageName)
        else 0

        if (resId != 0) {
            holder.binding.imgReceta.setImageResource(resId)
            holder.binding.imgReceta.setPadding(0, 0, 0, 0)
            holder.binding.imgReceta.setBackgroundColor(Color.TRANSPARENT)
        } else {
            val pad = (10 * ctx.resources.displayMetrics.density).toInt()
            holder.binding.imgReceta.setImageResource(R.drawable.ic_receta)
            holder.binding.imgReceta.setPadding(pad, pad, pad, pad)
            holder.binding.imgReceta.setBackgroundResource(R.color.naranja_suave)
        }
    }

    override fun getItemCount(): Int = lista.size

    // Actualiza la lista y refresca la vista
    fun actualizar(nueva: List<Receta>) {
        lista = nueva
        notifyDataSetChanged()
    }
}
