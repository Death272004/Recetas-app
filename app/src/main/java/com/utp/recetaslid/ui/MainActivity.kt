package com.utp.recetaslid.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.utp.recetaslid.adapter.RecetaAdapter
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.data.SessionManager
import com.utp.recetaslid.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: DBHelper
    private lateinit var sesion: SessionManager
    private lateinit var adapter: RecetaAdapter
    private var esInvitado = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)
        sesion = SessionManager(this)
        esInvitado = !sesion.haySesion()

        binding.txtSaludo.text = if (esInvitado) "Hola, Invitado" else "Hola, ${sesion.getNombre()}"
        binding.imgFotoPerfil.clipToOutline = true

        binding.btnSalir.text = if (esInvitado) "Entrar" else "Salir"
        binding.btnSalir.setOnClickListener {
            if (esInvitado) {
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                sesion.cerrarSesion()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        adapter = RecetaAdapter(db.listarRecetas()) { receta ->
            abrirDetalle(receta.id)
        }
        binding.recyclerSugeridas.layoutManager = LinearLayoutManager(this)
        binding.recyclerSugeridas.adapter = adapter

        binding.btnBuscar.setOnClickListener {
            val texto = binding.edtIngredientes.text.toString()
            val ingredientes = texto.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val intent = Intent(this, ResultadosActivity::class.java)
            intent.putExtra("ingredientes", ingredientes.toTypedArray())
            intent.putExtra("economicas", false)
            startActivity(intent)
        }

        binding.navHome.setOnClickListener { adapter.actualizar(db.listarRecetas()) }
        binding.navFeed.setOnClickListener { startActivity(Intent(this, FeedActivity::class.java)) }

        binding.navFavoritos.setOnClickListener {
            if (esInvitado) {
                Toast.makeText(this, "Inicia sesion para ver tus favoritos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(Intent(this, FavoritosActivity::class.java))
        }

        binding.navOtros.setOnClickListener { startActivity(Intent(this, OtrosActivity::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        adapter.actualizar(db.listarRecetas())
    }

    private fun abrirDetalle(recetaId: Int) {
        val intent = Intent(this, DetalleRecetaActivity::class.java)
        intent.putExtra("recetaId", recetaId)
        startActivity(intent)
    }
}
