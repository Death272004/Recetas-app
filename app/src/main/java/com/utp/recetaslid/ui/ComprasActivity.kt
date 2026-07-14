package com.utp.recetaslid.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.utp.recetaslid.adapter.CompraAdapter
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.data.SessionManager
import com.utp.recetaslid.databinding.ActivityComprasBinding

class ComprasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityComprasBinding
    private lateinit var db: DBHelper
    private lateinit var sesion: SessionManager
    private lateinit var adapter: CompraAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComprasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)
        sesion = SessionManager(this)

        if (!sesion.haySesion()) {
            Toast.makeText(this, "Inicia sesion para ver tu lista de compras", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        adapter = CompraAdapter(
            emptyList(),
            alMarcar = { item, marcado ->
                db.marcarComprado(item.id, marcado)
                refrescar()
            },
            alEliminar = { item ->
                db.eliminarCompra(item.id)
                refrescar()
            }
        )
        binding.recyclerCompras.layoutManager = LinearLayoutManager(this)
        binding.recyclerCompras.adapter = adapter

        binding.btnVolver.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        if (!sesion.haySesion()) return
        refrescar()
    }

    private fun refrescar() {
        val lista = db.listarCompras(sesion.getUsuarioId())
        adapter.actualizar(lista)
        binding.txtVacio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
        val total = lista.filter { it.comprado }.sumOf { it.precio }
        binding.txtTotal.text = "B/. ${"%.2f".format(total)}"
    }
}
