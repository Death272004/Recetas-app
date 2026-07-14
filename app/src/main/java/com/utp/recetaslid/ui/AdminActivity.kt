package com.utp.recetaslid.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.utp.recetaslid.adapter.RecetaAdapter
import com.utp.recetaslid.adapter.UsuarioAdapter
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.data.SessionManager
import com.utp.recetaslid.databinding.ActivityAdminBinding

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var db: DBHelper
    private lateinit var sesion: SessionManager
    private lateinit var usuarioAdapter: UsuarioAdapter
    private lateinit var reportadasAdapter: RecetaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)
        sesion = SessionManager(this)

        usuarioAdapter = UsuarioAdapter(emptyList()) { usuario ->
            AlertDialog.Builder(this)
                .setTitle("Eliminar usuario")
                .setMessage("¿Seguro que quieres eliminar a \"${usuario.nombre}\"? Esta accion no se puede deshacer.")
                .setPositiveButton("Eliminar") { _, _ ->
                    db.eliminarUsuario(usuario.id)
                    Toast.makeText(this, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                    refrescar()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
        binding.recyclerUsuarios.layoutManager = LinearLayoutManager(this)
        binding.recyclerUsuarios.adapter = usuarioAdapter

        reportadasAdapter = RecetaAdapter(emptyList()) { receta ->
            AlertDialog.Builder(this)
                .setTitle("Eliminar receta reportada")
                .setMessage("¿Seguro que quieres eliminar \"${receta.titulo}\"? Esta accion no se puede deshacer.")
                .setPositiveButton("Eliminar") { _, _ ->
                    db.eliminarReceta(receta.id)
                    Toast.makeText(this, "Receta eliminada", Toast.LENGTH_SHORT).show()
                    refrescar()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
        binding.recyclerReportadas.layoutManager = LinearLayoutManager(this)
        binding.recyclerReportadas.adapter = reportadasAdapter

        binding.btnSalir.setOnClickListener {
            sesion.cerrarSesion()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        refrescar()
    }

    private fun refrescar() {
        binding.txtTotalUsuarios.text = db.contarUsuarios().toString()
        binding.txtTotalRecetas.text = db.contarRecetas().toString()
        usuarioAdapter.actualizar(db.listarUsuarios())

        val reportadas = db.listarReportadas()
        reportadasAdapter.actualizar(reportadas)
        binding.txtSinReportes.visibility = if (reportadas.isEmpty()) View.VISIBLE else View.GONE
    }
}
