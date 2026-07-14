package com.utp.recetaslid.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.utp.recetaslid.adapter.AdminLogAdapter
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.databinding.ActivityAdminLogsBinding

class AdminLogsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminLogsBinding
    private lateinit var db: DBHelper
    private lateinit var adapter: AdminLogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminLogsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)
        adapter = AdminLogAdapter(emptyList())
        binding.recyclerLogs.layoutManager = LinearLayoutManager(this)
        binding.recyclerLogs.adapter = adapter

        binding.btnVolver.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        refrescar()
    }

    private fun refrescar() {
        val lista = db.listarLogs()
        adapter.actualizar(lista)
        binding.txtVacio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
    }
}
