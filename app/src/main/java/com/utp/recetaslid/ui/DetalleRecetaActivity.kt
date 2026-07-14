package com.utp.recetaslid.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.data.SessionManager
import com.utp.recetaslid.databinding.ActivityDetalleRecetaBinding
import com.utp.recetaslid.model.Receta

class DetalleRecetaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleRecetaBinding
    private lateinit var db: DBHelper
    private lateinit var sesion: SessionManager
    private var receta: Receta? = null
    private var ingredientesUsuario: List<String> = emptyList()
    private var esInvitado = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleRecetaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)
        sesion = SessionManager(this)
        esInvitado = !sesion.haySesion()

        val recetaId = intent.getIntExtra("recetaId", -1)
        ingredientesUsuario = intent.getStringArrayExtra("ingredientes")?.toList() ?: emptyList()
        receta = db.obtenerReceta(recetaId)

        val r = receta
        if (r == null) {
            Toast.makeText(this, "Receta no encontrada", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        mostrarReceta(r)
        cargarVideo(r)

        binding.btnVolver.setOnClickListener { finish() }

        binding.btnBuscarYouTube.setOnClickListener {
            val intent = Intent(this, YouTubeResultsActivity::class.java)
            intent.putExtra("query", r.titulo)
            startActivity(intent)
        }

        if (esInvitado) {
            binding.btnFavorito.visibility = View.GONE
            binding.btnCompras.visibility = View.GONE
        } else {
            actualizarBotonFavorito(r)
            binding.btnFavorito.setOnClickListener {
                val esFav = db.alternarFavorito(sesion.getUsuarioId(), r.id)
                Toast.makeText(
                    this,
                    if (esFav) "Agregada a favoritos" else "Quitada de favoritos",
                    Toast.LENGTH_SHORT
                ).show()
                actualizarBotonFavorito(r)
            }
            binding.btnCompras.setOnClickListener { anadirFaltantes(r) }
        }

        if (esInvitado) {
            binding.btnReportar.visibility = View.GONE
        } else {
            binding.btnReportar.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Reportar receta")
                    .setMessage("¿Quieres reportar \"${r.titulo}\" como inapropiada? Un administrador la revisara.")
                    .setPositiveButton("Reportar") { _, _ ->
                        db.reportarReceta(r.id)
                        Toast.makeText(this, "Receta reportada", Toast.LENGTH_SHORT).show()
                        binding.btnReportar.isEnabled = false
                        binding.btnReportar.text = "Reportada"
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
            if (r.reportada) {
                binding.btnReportar.isEnabled = false
                binding.btnReportar.text = "Reportada"
            }
        }
    }

    private fun mostrarReceta(r: Receta) {
        binding.txtTitulo.text = r.titulo
        binding.txtMeta.text = "B/. ${"%.2f".format(r.costo)} · ${r.tiempo} min"

        val faltan = db.faltantes(r, ingredientesUsuario)
        val texto = StringBuilder()
        for (ing in r.listaIngredientes()) {
            if (ingredientesUsuario.isEmpty()) {
                texto.append("• ").append(ing).append("\n")
            } else {
                val estado = if (faltan.contains(ing)) "(te falta)" else "(tienes)"
                texto.append("• ").append(ing).append("  ").append(estado).append("\n")
            }
        }
        binding.txtIngredientes.text = texto.toString().trim()
        binding.txtPasos.text = r.pasos
    }

    private fun actualizarBotonFavorito(r: Receta) {
        val esFav = db.esFavorito(sesion.getUsuarioId(), r.id)
        binding.btnFavorito.text = if (esFav) "Quitar de favoritos" else "Agregar a favoritos"
    }

    private fun cargarVideo(r: Receta) {
        if (r.videoUrl.isNotEmpty()) {
            binding.btnVerVideo.visibility = View.VISIBLE
            binding.txtVideoNoDisponible.visibility = View.GONE
            binding.btnVerVideo.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(r.videoUrl)))
            }
        } else {
            binding.btnVerVideo.visibility = View.GONE
            binding.txtVideoNoDisponible.visibility = View.VISIBLE
        }
    }

    private fun anadirFaltantes(r: Receta) {
        val faltan = db.faltantes(r, ingredientesUsuario)
        if (faltan.isEmpty()) {
            Toast.makeText(this, "No te falta ningun ingrediente", Toast.LENGTH_SHORT).show()
            return
        }
        for (ing in faltan) {
            db.agregarACompras(sesion.getUsuarioId(), ing, 0.40)
        }
        Toast.makeText(this, "${faltan.size} ingredientes anadidos a compras", Toast.LENGTH_SHORT).show()
    }
}
