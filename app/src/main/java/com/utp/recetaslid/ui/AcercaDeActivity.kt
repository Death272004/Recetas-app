package com.utp.recetaslid.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.utp.recetaslid.databinding.ActivityAcercaDeBinding

class AcercaDeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAcercaDeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAcercaDeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnVolver.setOnClickListener { finish() }
    }
}
