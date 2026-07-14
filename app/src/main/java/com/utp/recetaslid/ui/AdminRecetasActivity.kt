package com.utp.recetaslid.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.utp.recetaslid.R
import com.utp.recetaslid.adapter.AdminRecetaAdapter
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.data.SessionManager
import com.utp.recetaslid.databinding.ActivityAdminRecetasBinding

class AdminRecetasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminRecetasBinding
    private lateinit var db: DBHelper
    private lateinit var sesion: SessionManager
    private lateinit var adapter: AdminRecetaAdapter
    private var filtroActual = "todas"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminRecetasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)
        sesion = SessionManager(this)

        adapter = AdminRecetaAdapter(
            emptyList(),
            obtenerNombreAutor = { db.obtenerNombreUsuario(it) },
            onDestacar = { r ->
                db.destacarReceta(r.id, !r.destacada)
                val accion = if (r.destacada) "Quitar destacado" else "Destacar"
                db.registrarLog(sesion.getUsuarioId(), "$accion receta", "\"${r.titulo}\"")
                Toast.makeText(this, if (r.destacada) "Destacado quitado" else "Receta destacada", Toast.LENGTH_SHORT).show()
                refrescar()
            },
            onOcultar = { r ->
                db.ocultarReceta(r.id, !r.oculta)
                val accion = if (r.oculta) "Mostrar" else "Ocultar"
                db.registrarLog(sesion.getUsuarioId(), "$accion receta", "\"${r.titulo}\"")
                Toast.makeText(this, if (r.oculta) "Receta visible" else "Receta oculta", Toast.LENGTH_SHORT).show()
                refrescar()
            },
            onQuitarReporte = { r ->
                db.quitarReporte(r.id)
                db.registrarLog(sesion.getUsuarioId(), "Quitar reporte", "\"${r.titulo}\"")
                Toast.makeText(this, "Reporte quitado", Toast.LENGTH_SHORT).show()
                refrescar()
            },
            onEliminar = { r ->
                AlertDialog.Builder(this)
                    .setTitle("Eliminar receta")
                    .setMessage("Seguro que quieres eliminar \"${r.titulo}\"? Esta accion no se puede deshacer.")
                    .setPositiveButton("Eliminar") { _, _ ->
                        db.moverAPapelera("receta", "ID:${r.id} Titulo:${r.titulo}", sesion.getUsuarioId())
                        db.eliminarReceta(r.id)
                        db.registrarLog(sesion.getUsuarioId(), "Eliminacion de receta", "\"${r.titulo}\"")
                        Toast.makeText(this, "Receta eliminada", Toast.LENGTH_SHORT).show()
                        refrescar()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )
        binding.recyclerRecetas.layoutManager = LinearLayoutManager(this)
        binding.recyclerRecetas.adapter = adapter

        binding.btnVolver.setOnClickListener { finish() }

        binding.btnFiltroTodas.setOnClickListener { setFiltro("todas") }
        binding.btnFiltroReportadas.setOnClickListener { setFiltro("reportadas") }
        binding.btnFiltroOcultas.setOnClickListener { setFiltro("ocultas") }

        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { refrescar() }
        })
    }

    override fun onResume() {
        super.onResume()
        refrescar()
    }

    private fun setFiltro(filtro: String) {
        filtroActual = filtro
        actualizarBotonesFiltro()
        refrescar()
    }

    private fun actualizarBotonesFiltro() {
        binding.btnFiltroTodas.backgroundTintList = getColorStateList(
            if (filtroActual == "todas") R.color.naranja else R.color.gris_claro
        )
        binding.btnFiltroTodas.setTextColor(getColor(
            if (filtroActual == "todas") R.color.blanco else R.color.negro
        ))
        binding.btnFiltroReportadas.backgroundTintList = getColorStateList(
            if (filtroActual == "reportadas") R.color.naranja else R.color.gris_claro
        )
        binding.btnFiltroReportadas.setTextColor(getColor(
            if (filtroActual == "reportadas") R.color.blanco else R.color.negro
        ))
        binding.btnFiltroOcultas.backgroundTintList = getColorStateList(
            if (filtroActual == "ocultas") R.color.naranja else R.color.gris_claro
        )
        binding.btnFiltroOcultas.setTextColor(getColor(
            if (filtroActual == "ocultas") R.color.blanco else R.color.negro
        ))
    }

    private fun refrescar() {
        val query = binding.etBuscar.text.toString().trim()
        var lista = if (query.isEmpty()) db.listarRecetasAdmin()
        else db.buscarRecetasAdmin(query)

        lista = when (filtroActual) {
            "reportadas" -> lista.filter { it.reportada }
            "ocultas" -> lista.filter { it.oculta }
            else -> lista
        }

        adapter.actualizar(lista)
        binding.txtConteo.text = "${lista.size} recetas"
        binding.txtVacio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
    }
}
