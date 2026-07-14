package com.utp.recetaslid.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.utp.recetaslid.adapter.YouTubeAdapter
import com.utp.recetaslid.data.AppConfig
import com.utp.recetaslid.data.YouTubeHelper
import com.utp.recetaslid.databinding.ActivityYoutubeResultsBinding
import java.net.URLEncoder

class YouTubeResultsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityYoutubeResultsBinding
    private lateinit var adapter: YouTubeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYoutubeResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val query = intent.getStringExtra("query") ?: ""
        binding.txtBusqueda.text = "Resultados para: $query"
        binding.btnVolver.setOnClickListener { finish() }

        adapter = YouTubeAdapter(emptyList())
        binding.recyclerVideos.layoutManager = LinearLayoutManager(this)
        binding.recyclerVideos.adapter = adapter

        if (AppConfig.YOUTUBE_API_KEY.isEmpty()) {
            val encoded = URLEncoder.encode("receta $query", "UTF-8")
            startActivity(
                Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/results?search_query=$encoded"))
            )
            finish()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        Thread {
            try {
                val resultados = YouTubeHelper.buscar(query)
                runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    if (resultados.isEmpty()) {
                        binding.txtVacio.visibility = View.VISIBLE
                    } else {
                        adapter.actualizar(resultados)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Error al buscar videos", Toast.LENGTH_SHORT).show()
                    val encoded = URLEncoder.encode("receta $query", "UTF-8")
                    startActivity(
                        Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.youtube.com/results?search_query=$encoded"))
                    )
                    finish()
                }
            }
        }.start()
    }
}
