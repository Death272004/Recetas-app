package com.utp.recetaslid.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
        binding.txtRolPerfil.text = "${sesion.getRol().replaceFirstChar { it.uppercase() }} · RecetasLID"
        binding.txtRecetaCount.text = db.contarRecetasDeUsuario(userId).toString()
        binding.txtFavoritoCount.text = db.contarFavoritosDeUsuario(userId).toString()
        binding.txtComprasCount.text = db.contarComprasDeUsuario(userId).toString()
    }
}
