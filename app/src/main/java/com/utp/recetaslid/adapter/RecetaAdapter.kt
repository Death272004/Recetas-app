package com.utp.recetaslid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.utp.recetaslid.databinding.ItemRecetaBinding
import com.utp.recetaslid.model.Receta
import com.utp.recetaslid.util.ImagenUtil

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

        // Carga la imagen del plato si existe (drawable o foto propia), o muestra el icono generico
        ImagenUtil.mostrar(holder.binding.imgReceta, receta.imagen)
    }

    override fun getItemCount(): Int = lista.size

    // Actualiza la lista y refresca la vista
    fun actualizar(nueva: List<Receta>) {
        lista = nueva
        notifyDataSetChanged()
    }
}
