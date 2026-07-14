package com.utp.recetaslid.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.utp.recetaslid.data.SessionManager
import com.utp.recetaslid.databinding.ActivitySplashBinding

// Pantalla de bienvenida. Muestra el logo unos segundos y decide a donde ir
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sesion = SessionManager(this)

        // Esperamos 1.5 segundos antes de continuar
        Handler(Looper.getMainLooper()).postDelayed({
            val destino = when {
                sesion.haySesion() && sesion.esAdmin() -> AdminActivity::class.java
                else -> MainActivity::class.java
            }
            startActivity(Intent(this, destino))
            finish()
        }, 1500)
    }
}
