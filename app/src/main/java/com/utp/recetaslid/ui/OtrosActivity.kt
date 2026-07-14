package com.utp.recetaslid.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.utp.recetaslid.data.SessionManager
import com.utp.recetaslid.databinding.ActivityOtrosBinding

class OtrosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtrosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtrosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sesion = SessionManager(this)
        val esInvitado = !sesion.haySesion()

        binding.btnVolver.setOnClickListener { finish() }

        if (esInvitado) {
            binding.btnPerfil.setOnClickListener {
                Toast.makeText(this, "Inicia sesion para ver tu perfil", Toast.LENGTH_SHORT).show()
            }
            binding.btnCompras.setOnClickListener {
                Toast.makeText(this, "Inicia sesion para ver tu lista de compras", Toast.LENGTH_SHORT).show()
            }
            binding.btnCrear.setOnClickListener {
                Toast.makeText(this, "Inicia sesion para crear recetas", Toast.LENGTH_SHORT).show()
            }
        } else {
            binding.btnPerfil.setOnClickListener {
                startActivity(Intent(this, PerfilActivity::class.java))
            }
            binding.btnCompras.setOnClickListener {
                startActivity(Intent(this, ComprasActivity::class.java))
            }
            binding.btnCrear.setOnClickListener {
                startActivity(Intent(this, CrearRecetaActivity::class.java))
            }
        }
    }
}
