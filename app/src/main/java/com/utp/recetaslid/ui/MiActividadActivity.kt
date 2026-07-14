package com.utp.recetaslid.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.utp.recetaslid.adapter.RecetaAdapter
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.data.SessionManager
import com.utp.recetaslid.databinding.ActivityMiActividadBinding

class MiActividadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMiActividadBinding
    private lateinit var db: DBHelper
    private lateinit var sesion: SessionManager
    private lateinit var adapter: RecetaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMiActividadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)
        sesion = SessionManager(this)

        adapter = RecetaAdapter(emptyList()) { receta ->
            AlertDialog.Builder(this)
                .setTitle(receta.titulo)
                .setItems(arrayOf("Ver detalle", "Eliminar")) { _, which ->
                    when (which) {
                        0 -> {
                            val i = Intent(this, DetalleRecetaActivity::class.java)
                            i.putExtra("recetaId", receta.id)
                            startActivity(i)
                        }
                        1 -> {
                            AlertDialog.Builder(this)
                                .setTitle("Eliminar receta")
                                .setMessage("¿Seguro que quieres eliminar \"${receta.titulo}\"?")
                                .setPositiveButton("Eliminar") { _, _ ->
                                    db.eliminarReceta(receta.id)
                                    Toast.makeText(this, "Receta eliminada", Toast.LENGTH_SHORT).show()
                                    cargarRecetas()
                                }
                                .setNegativeButton("Cancelar", null)
                                .show()
                        }
                    }
                }
                .show()
        }

        binding.recyclerMisRecetas.layoutManager = LinearLayoutManager(this)
        binding.recyclerMisRecetas.adapter = adapter

        binding.btnVolver.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        cargarRecetas()
    }

    private fun cargarRecetas() {
        val recetas = db.listarRecetasDeUsuario(sesion.getUsuarioId())
        adapter.actualizar(recetas)
        binding.txtVacio.visibility = if (recetas.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerMisRecetas.visibility = if (recetas.isEmpty()) View.GONE else View.VISIBLE
    }
}
