package com.utp.recetaslid.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.data.SessionManager
import com.utp.recetaslid.databinding.ActivityLogrosBinding

class LogrosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogrosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogrosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = DBHelper(this)
        val sesion = SessionManager(this)
        val userId = sesion.getUsuarioId()

        val recetas = db.contarRecetasDeUsuario(userId)
        val favs = db.contarFavoritosDeUsuario(userId)
        val compras = db.contarComprasDeUsuario(userId)
        val comentarios = db.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM comentarios WHERE usuarioId = ?", arrayOf(userId.toString())
        ).use { c -> c.moveToFirst(); c.getInt(0) }

        data class Logro(val alcanzado: Boolean, val icon: android.widget.TextView, val desc: android.widget.TextView)

        val logros = listOf(
            Logro(recetas >= 1, binding.iconReceta1, binding.descReceta1),
            Logro(recetas >= 5, binding.iconReceta5, binding.descReceta5),
            Logro(recetas >= 10, binding.iconReceta10, binding.descReceta10),
            Logro(favs >= 1, binding.iconFav1, binding.descFav1),
            Logro(favs >= 5, binding.iconFav5, binding.descFav5),
            Logro(favs >= 10, binding.iconFav10, binding.descFav10),
            Logro(comentarios >= 1, binding.iconComentario1, binding.descComentario1),
            Logro(compras >= 1, binding.iconCompra1, binding.descCompra1)
        )

        var desbloqueados = 0
        for (l in logros) {
            if (l.alcanzado) {
                l.icon.text = "🏆"
                desbloqueados++
            } else {
                l.icon.text = "🔒"
                l.icon.alpha = 0.4f
                l.desc.alpha = 0.5f
            }
        }

        binding.txtProgreso.text = "$desbloqueados/8"
        binding.progressLogros.progress = desbloqueados

        binding.btnVolver.setOnClickListener { finish() }
    }
}
