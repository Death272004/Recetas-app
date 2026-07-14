package com.utp.recetaslid.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.utp.recetaslid.R
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.data.SessionManager
import com.utp.recetaslid.databinding.ActivityPerfilBinding

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private lateinit var db: DBHelper
    private lateinit var sesion: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)
        sesion = SessionManager(this)

        if (!sesion.haySesion()) {
            Toast.makeText(this, "Inicia sesion para ver tu perfil", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.imgPerfilGrande.clipToOutline = true
        binding.btnVolver.setOnClickListener { finish() }

        binding.menuEditarPerfil.setOnClickListener {
            startActivity(Intent(this, EditarPerfilActivity::class.java))
        }

        binding.menuNotificaciones.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Notificaciones")
                .setMessage("Las notificaciones push estan activadas.\n\nRecibiras alertas cuando:\n• Se publique una nueva receta\n• Alguien marque tu receta como favorita\n• Haya ofertas en ingredientes")
                .setPositiveButton("Entendido", null)
                .show()
        }

        binding.menuPrivacidad.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Privacidad")
                .setMessage("Tu informacion esta protegida.\n\n• Tu correo no es visible para otros usuarios\n• Tus favoritos son privados\n• Tu lista de compras es privada\n• Puedes eliminar tu cuenta contactando al administrador")
                .setPositiveButton("Entendido", null)
                .show()
        }

        binding.menuLogros.setOnClickListener {
            val userId = sesion.getUsuarioId()
            val recetas = db.contarRecetasDeUsuario(userId)
            val favs = db.contarFavoritosDeUsuario(userId)

            val logros = StringBuilder()
            logros.append(if (recetas >= 1) "✅" else "⬜").append(" Primera receta — Crea tu primera receta\n")
            logros.append(if (recetas >= 5) "✅" else "⬜").append(" Chef activo — Crea 5 recetas\n")
            logros.append(if (recetas >= 10) "✅" else "⬜").append(" Chef experto — Crea 10 recetas\n")
            logros.append(if (favs >= 1) "✅" else "⬜").append(" Explorador — Marca tu primer favorito\n")
            logros.append(if (favs >= 5) "✅" else "⬜").append(" Coleccionista — Marca 5 favoritos\n")
            logros.append(if (favs >= 10) "✅" else "⬜").append(" Gourmet — Marca 10 favoritos")

            AlertDialog.Builder(this)
                .setTitle("Mis Logros")
                .setMessage(logros.toString())
                .setPositiveButton("Cerrar", null)
                .show()
        }

        binding.menuActividad.setOnClickListener {
            startActivity(Intent(this, MiActividadActivity::class.java))
        }

        binding.menuAcercaDe.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("RECETAS LID")
                .setMessage("Version 1.0\n\nAplicacion de recetas de cocina.\nBusca recetas por ingredientes, guarda tus favoritas y crea tus propias recetas.\n\nDesarrollado con Android Studio + Kotlin\nBase de datos: SQLite")
                .setPositiveButton("Cerrar", null)
                .show()
        }

        binding.menuAyuda.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Ayuda y Soporte")
                .setMessage("¿Como usar RECETAS LID?\n\n• Buscar: Ingresa ingredientes separados por coma en la pantalla principal\n• Favoritos: Toca el boton de favoritos en cualquier receta\n• Crear: Ve a Otros > Crear receta para publicar la tuya\n• Compras: Anade ingredientes faltantes desde el detalle de una receta\n• Feed: Ve las recetas mas recientes de la comunidad\n\nSi tienes problemas, contacta al administrador.")
                .setPositiveButton("Entendido", null)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDatos()
    }

    private fun cargarDatos() {
        val userId = sesion.getUsuarioId()
        binding.txtNombrePerfil.text = sesion.getNombre()
        binding.txtRolPerfil.text = "${sesion.getRol().replaceFirstChar { it.uppercase() }} · RecetasLID"
        binding.txtRecetaCount.text = db.contarRecetasDeUsuario(userId).toString()
        binding.txtFavoritoCount.text = db.contarFavoritosDeUsuario(userId).toString()
        binding.txtComprasCount.text = db.contarComprasDeUsuario(userId).toString()
    }
}
