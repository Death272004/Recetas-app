package com.utp.recetaslid.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.data.SessionManager
import com.utp.recetaslid.databinding.ActivityAdminBinding

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var db: DBHelper
    private lateinit var sesion: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)
        sesion = SessionManager(this)

        binding.btnSalir.setOnClickListener {
            sesion.cerrarSesion()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btnGestionUsuarios.setOnClickListener {
            startActivity(Intent(this, AdminUsuariosActivity::class.java))
        }
        binding.btnGestionRecetas.setOnClickListener {
            startActivity(Intent(this, AdminRecetasActivity::class.java))
        }
        binding.btnGestionComentarios.setOnClickListener {
            startActivity(Intent(this, AdminComentariosActivity::class.java))
        }
        binding.btnRegistroActividad.setOnClickListener {
            startActivity(Intent(this, AdminLogsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        refrescarStats()
    }

    private fun refrescarStats() {
        binding.txtTotalUsuarios.text = db.contarUsuarios().toString()
        binding.txtUsuariosActivos.text = db.contarUsuariosActivos().toString()
        binding.txtTotalRecetas.text = db.contarRecetas().toString()
        binding.txtTotalComentarios.text = db.contarTodosComentarios().toString()
        binding.txtReportesPendientes.text = db.contarReportesPendientes().toString()
        binding.txtTotalLogs.text = db.contarLogs().toString()
    }
}
