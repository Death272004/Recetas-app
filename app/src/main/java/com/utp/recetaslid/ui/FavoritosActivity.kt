package com.utp.recetaslid.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.utp.recetaslid.adapter.RecetaAdapter
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.data.SessionManager
import com.utp.recetaslid.databinding.ActivityFavoritosBinding

class FavoritosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritosBinding
    private lateinit var db: DBHelper
    private lateinit var sesion: SessionManager
    private lateinit var adapter: RecetaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)
        sesion = SessionManager(this)

        if (!sesion.haySesion()) {
            Toast.makeText(this, "Inicia sesion para ver tus favoritos", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        adapter = RecetaAdapter(emptyList()) { receta ->
            val i = Intent(this, DetalleRecetaActivity::class.java)
            i.putExtra("recetaId", receta.id)
            startActivity(i)
        }
        binding.recyclerFavoritos.layoutManager = LinearLayoutManager(this)
        binding.recyclerFavoritos.adapter = adapter

        binding.btnVolver.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        if (!sesion.haySesion()) return
        val favoritos = db.listarFavoritos(sesion.getUsuarioId())
        adapter.actualizar(favoritos)
        binding.txtVacio.visibility = if (favoritos.isEmpty()) View.VISIBLE else View.GONE
    }
}
