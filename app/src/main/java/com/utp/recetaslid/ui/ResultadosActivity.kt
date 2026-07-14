package com.utp.recetaslid.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.utp.recetaslid.adapter.RecetaAdapter
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.databinding.ActivityResultadosBinding

class ResultadosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultadosBinding
    private lateinit var db: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultadosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)

        val ingredientes = intent.getStringArrayExtra("ingredientes")?.toList() ?: emptyList()
        val economicas = intent.getBooleanExtra("economicas", false)

        val textoFiltro = if (ingredientes.isEmpty()) "Todas las recetas"
        else "Con: ${ingredientes.joinToString(", ")}"
        binding.txtFiltro.text = textoFiltro

        val resultados = db.buscarPorIngredientes(ingredientes, economicas)

        if (resultados.isEmpty()) {
            binding.txtVacio.visibility = android.view.View.VISIBLE
        }

        val adapter = RecetaAdapter(resultados) { receta ->
            val i = Intent(this, DetalleRecetaActivity::class.java)
            i.putExtra("recetaId", receta.id)
            i.putExtra("ingredientes", ingredientes.toTypedArray())
            startActivity(i)
        }
        binding.recyclerResultados.layoutManager = LinearLayoutManager(this)
        binding.recyclerResultados.adapter = adapter

        binding.btnVolver.setOnClickListener { finish() }
    }
}
