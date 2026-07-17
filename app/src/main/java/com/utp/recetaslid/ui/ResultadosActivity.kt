package com.utp.recetaslid.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.utp.recetaslid.adapter.RecetaAdapter
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.data.SessionManager
import com.utp.recetaslid.databinding.ActivityResultadosBinding

class ResultadosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultadosBinding
    private lateinit var db: DBHelper
    private lateinit var sesion: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultadosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)
        sesion = SessionManager(this)

        val ingredientes = intent.getStringArrayExtra("ingredientes")?.toList() ?: emptyList()
        val economicas = intent.getBooleanExtra("economicas", false)

        val textoFiltro = if (ingredientes.isEmpty()) "Todas las recetas"
        else "Con: ${ingredientes.joinToString(", ")}"
        // Dejamos claro cuando el filtro de economicas esta activo (RF-05)
        binding.txtFiltro.text = if (economicas) "$textoFiltro \u00B7 solo economicas" else textoFiltro

        val resultados = db.buscarPorIngredientes(ingredientes, economicas)

        if (resultados.isEmpty()) {
            binding.txtVacio.visibility = android.view.View.VISIBLE
        }

        val adapter = RecetaAdapter(resultados) { receta ->
            // El invitado debe iniciar sesion para abrir el detalle
            if (!sesion.haySesion()) {
                LoginRapido.mostrar(this, "Entra a tu cuenta para ver esta receta") {
                    verDetalle(receta.id, ingredientes)
                }
            } else {
                verDetalle(receta.id, ingredientes)
            }
        }
        binding.recyclerResultados.layoutManager = LinearLayoutManager(this)
        binding.recyclerResultados.adapter = adapter

        binding.btnVolver.setOnClickListener { finish() }
    }

    private fun verDetalle(recetaId: Int, ingredientes: List<String>) {
        val i = Intent(this, DetalleRecetaActivity::class.java)
        i.putExtra("recetaId", recetaId)
        i.putExtra("ingredientes", ingredientes.toTypedArray())
        startActivity(i)
    }
}
