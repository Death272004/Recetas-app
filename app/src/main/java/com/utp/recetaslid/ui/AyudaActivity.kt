package com.utp.recetaslid.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.utp.recetaslid.databinding.ActivityAyudaBinding

class AyudaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAyudaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAyudaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnVolver.setOnClickListener { finish() }

        val faqs = listOf(
            binding.faq1 to binding.faq1Resp,
            binding.faq2 to binding.faq2Resp,
            binding.faq3 to binding.faq3Resp,
            binding.faq4 to binding.faq4Resp,
            binding.faq5 to binding.faq5Resp,
            binding.faq6 to binding.faq6Resp
        )

        for ((container, respuesta) in faqs) {
            container.setOnClickListener {
                respuesta.visibility = if (respuesta.visibility == View.GONE) View.VISIBLE else View.GONE
            }
        }
    }
}
