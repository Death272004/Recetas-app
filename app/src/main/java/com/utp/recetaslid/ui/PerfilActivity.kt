package com.utp.recetaslid.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.data.SessionManager
import com.utp.recetaslid.databinding.ActivityPerfilBinding
import com.utp.recetaslid.util.ImagenUtil

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
        binding.txtInicialPerfilGrande.clipToOutline = true
        binding.btnVolver.setOnClickListener { finish() }

        binding.menuEditarPerfil.setOnClickListener {
            startActivity(Intent(this, EditarPerfilActivity::class.java))
        }
        binding.menuNotificaciones.setOnClickListener {
            startActivity(Intent(this, NotificacionesActivity::class.java))
        }
        binding.menuPrivacidad.setOnClickListener {
            startActivity(Intent(this, PrivacidadActivity::class.java))
        }
        binding.menuLogros.setOnClickListener {
            startActivity(Intent(this, LogrosActivity::class.java))
        }
        binding.menuActividad.setOnClickListener {
            startActivity(Intent(this, MiActividadActivity::class.java))
        }
        binding.menuAcercaDe.setOnClickListener {
            startActivity(Intent(this, AcercaDeActivity::class.java))
        }
        binding.menuAyuda.setOnClickListener {
            startActivity(Intent(this, AyudaActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDatos()
    }

    private fun cargarDatos() {
        val userId = sesion.getUsuarioId()
        binding.txtNombrePerfil.text = sesion.getNombre()
        mostrarAvatar(userId)
        binding.txtRolPerfil.text = "${sesion.getRol().replaceFirstChar { it.uppercase() }} · RecetasLID"
        binding.txtRecetaCount.text = db.contarRecetasDeUsuario(userId).toString()
        binding.txtFavoritoCount.text = db.contarFavoritosDeUsuario(userId).toString()
        binding.txtComprasCount.text = db.contarComprasDeUsuario(userId).toString()
    }

    // Solo las cuentas que tienen una foto guardada la muestran.
    // El resto ve la inicial de su nombre, igual que en el feed y en los perfiles publicos.
    private fun mostrarAvatar(userId: Int) {
        val usuario = db.obtenerUsuario(userId)
        val foto = usuario?.foto ?: ""
        val nombre = usuario?.nombre ?: sesion.getNombre()
        // Solo hay foto si la cuenta tiene una guardada y ademas se pudo cargar
        val hayFoto = foto.isNotEmpty() &&
            ImagenUtil.mostrar(binding.imgPerfilGrande, foto, redondeado = true)
        binding.imgPerfilGrande.visibility = if (hayFoto) View.VISIBLE else View.GONE
        binding.txtInicialPerfilGrande.visibility = if (hayFoto) View.GONE else View.VISIBLE
        if (!hayFoto) binding.txtInicialPerfilGrande.text = nombre.firstOrNull()?.uppercase() ?: "?"
    }
}
