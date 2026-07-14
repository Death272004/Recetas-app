package com.utp.recetaslid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.utp.recetaslid.data.ItemCompra
import com.utp.recetaslid.databinding.ItemCompraBinding

// Adaptador para la lista de compras
class CompraAdapter(
    private var lista: List<ItemCompra>,
    private val alMarcar: (ItemCompra, Boolean) -> Unit,
    private val alEliminar: (ItemCompra) -> Unit
) : RecyclerView.Adapter<CompraAdapter.CompraViewHolder>() {

    inner class CompraViewHolder(val binding: ItemCompraBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompraViewHolder {
        val binding = ItemCompraBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CompraViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CompraViewHolder, position: Int) {
        val item = lista[position]
        holder.binding.txtItem.text = item.item
        holder.binding.txtPrecio.text = "B/. ${"%.2f".format(item.precio)}"
        // Evitamos disparar el listener al reciclar la vista
        holder.binding.checkComprado.setOnCheckedChangeListener(null)
        holder.binding.checkComprado.isChecked = item.comprado
        holder.binding.checkComprado.setOnCheckedChangeListener { _, isChecked ->
            alMarcar(item, isChecked)
        }
        holder.binding.btnQuitar.setOnClickListener { alEliminar(item) }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizar(nueva: List<ItemCompra>) {
        lista = nueva
        notifyDataSetChanged()
    }
}
