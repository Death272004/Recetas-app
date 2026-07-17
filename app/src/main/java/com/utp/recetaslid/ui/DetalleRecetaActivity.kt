package com.utp.recetaslid.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.utp.recetaslid.R
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.data.SessionManager
import com.utp.recetaslid.databinding.ActivityDetalleRecetaBinding
import com.utp.recetaslid.model.Receta
import com.utp.recetaslid.util.ImagenUtil

class DetalleRecetaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleRecetaBinding
    private lateinit var db: DBHelper
    private lateinit var sesion: SessionManager
    private var receta: Receta? = null
    private var ingredientesUsuario: List<String> = emptyList()
    private var esInvitado = false
    private var videoVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleRecetaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.imgRecetaDetalle.clipToOutline = true

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
        configurarVideo(r)

        binding.btnVolver.setOnClickListener { finish() }

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
                    .setMessage("Quieres reportar \"${r.titulo}\" como inapropiada? Un administrador la revisara.")
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

        // Carga la imagen del plato (drawable o foto propia) con esquinas redondeadas
        ImagenUtil.mostrar(
            binding.imgRecetaDetalle, r.imagen,
            redondeado = true, fondoVacio = R.drawable.bg_imagen_receta, paddingVacioDp = 24
        )

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

    private fun configurarVideo(r: Receta) {
        val videoId = extraerVideoId(r.videoUrl)
        if (videoId != null) {
            binding.btnVerVideo.visibility = View.VISIBLE
            binding.txtVideoNoDisponible.visibility = View.GONE

            binding.btnVerVideo.setOnClickListener {
                if (!videoVisible) {
                    cargarVideoEmbebido(videoId)
                    binding.videoContainer.visibility = View.VISIBLE
                    binding.btnVerVideo.text = "▲  Ocultar video"
                    videoVisible = true
                } else {
                    binding.webVideo.loadUrl("about:blank")
                    binding.videoContainer.visibility = View.GONE
                    binding.btnVerVideo.text = "▶  Ver tutorial"
                    videoVisible = false
                }
            }
        } else if (r.videoUrl.isNotEmpty()) {
            binding.btnVerVideo.visibility = View.VISIBLE
            binding.txtVideoNoDisponible.visibility = View.GONE
            binding.btnVerVideo.setOnClickListener {
                if (!videoVisible) {
                    cargarVideoWeb(r.videoUrl)
                    binding.videoContainer.visibility = View.VISIBLE
                    binding.btnVerVideo.text = "▲  Ocultar video"
                    videoVisible = true
                } else {
                    binding.webVideo.loadUrl("about:blank")
                    binding.videoContainer.visibility = View.GONE
                    binding.btnVerVideo.text = "▶  Ver tutorial"
                    videoVisible = false
                }
            }
        } else {
            binding.btnVerVideo.visibility = View.GONE
            binding.txtVideoNoDisponible.visibility = View.VISIBLE
        }
    }

    // Altura del recuadro segun lo que se muestre:
    // un video incrustado usa proporcion 16:9, mientras que la pagina de YouTube
    // necesita mucho mas alto para poder ver y deslizar la lista de resultados.
    private val ALTO_VIDEO_DP = 220
    private val ALTO_BUSQUEDA_DP = 520

    // Cambia el alto del recuadro del video
    private fun ajustarAltoVideo(dp: Int) {
        val params = binding.videoContainer.layoutParams
        params.height = (dp * resources.displayMetrics.density).toInt()
        binding.videoContainer.layoutParams = params
    }

    // El WebView esta dentro de un ScrollView y, por defecto, el ScrollView se queda
    // con los gestos verticales: por eso la pagina de YouTube no se podia deslizar.
    // Con esto le pedimos al ScrollView que no intercepte el gesto mientras el dedo
    // esta sobre el WebView, de modo que la lista de videos si se pueda recorrer.
    @SuppressLint("ClickableViewAccessibility")
    private fun permitirScrollDentroDelVideo(activar: Boolean) {
        if (!activar) {
            binding.webVideo.setOnTouchListener(null)
            return
        }
        binding.webVideo.setOnTouchListener { v, _ ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
    }

    private fun cargarVideoEmbebido(videoId: String) {
        val webView = binding.webVideo
        val progress = binding.progressVideo
        progress.visibility = View.VISIBLE

        // Un solo video: basta con la proporcion 16:9 y no hace falta desplazarse
        ajustarAltoVideo(ALTO_VIDEO_DP)
        permitirScrollDentroDelVideo(false)

        webView.settings.javaScriptEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.settings.domStorageEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                progress.visibility = View.GONE
            }
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                progress.visibility = View.GONE
                Toast.makeText(this@DetalleRecetaActivity, "Error al cargar el video", Toast.LENGTH_SHORT).show()
            }
        }
        webView.webChromeClient = WebChromeClient()

        val html = """
            <html><head>
            <meta name="viewport" content="width=device-width,initial-scale=1">
            <style>*{margin:0;padding:0}body{background:#000}
            .video{position:relative;width:100%;padding-bottom:56.25%;height:0}
            .video iframe{position:absolute;top:0;left:0;width:100%;height:100%;border:0}
            </style></head><body>
            <div class="video">
            <iframe src="https://www.youtube.com/embed/$videoId?autoplay=1&rel=0&modestbranding=1&playsinline=1"
            allow="autoplay;encrypted-media;fullscreen" allowfullscreen></iframe>
            </div></body></html>
        """.trimIndent()
        webView.loadData(html, "text/html", "UTF-8")
    }

    private fun cargarVideoWeb(url: String) {
        val webView = binding.webVideo
        val progress = binding.progressVideo
        progress.visibility = View.VISIBLE

        // Aqui se abre la pagina de YouTube con la lista de resultados:
        // le damos mas alto y habilitamos el deslizamiento dentro del recuadro
        ajustarAltoVideo(ALTO_BUSQUEDA_DP)
        permitirScrollDentroDelVideo(true)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, u: String?) {
                progress.visibility = View.GONE
            }
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                progress.visibility = View.GONE
                Toast.makeText(this@DetalleRecetaActivity, "Error al cargar el video", Toast.LENGTH_SHORT).show()
            }
        }
        webView.webChromeClient = WebChromeClient()
        webView.loadUrl(url)
    }

    private fun extraerVideoId(url: String): String? {
        if (url.isEmpty()) return null
        val patterns = listOf(
            Regex("youtube\\.com/watch\\?v=([a-zA-Z0-9_-]{11})"),
            Regex("youtu\\.be/([a-zA-Z0-9_-]{11})"),
            Regex("youtube\\.com/embed/([a-zA-Z0-9_-]{11})")
        )
        for (p in patterns) {
            val match = p.find(url)
            if (match != null) return match.groupValues[1]
        }
        return null
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

    override fun onDestroy() {
        binding.webVideo.loadUrl("about:blank")
        binding.webVideo.destroy()
        super.onDestroy()
    }
}
