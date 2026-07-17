package com.utp.recetaslid.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.utp.recetaslid.adapter.RecetaAdapter
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.data.SessionManager
import com.utp.recetaslid.data.ThemeManager
import com.utp.recetaslid.databinding.ActivityMainBinding
import com.utp.recetaslid.util.ImagenUtil

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: DBHelper
    private lateinit var sesion: SessionManager
    private lateinit var adapter: RecetaAdapter
    private var esInvitado = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)
        sesion = SessionManager(this)
        actualizarCabecera()
        binding.imgFotoPerfil.clipToOutline = true
        binding.txtInicialPerfil.clipToOutline = true
        val modoOscuro = ThemeManager.esModoOscuro(this)
        binding.switchTema.isChecked = modoOscuro
        binding.txtTema.text = if (modoOscuro) "Oscuro" else "Claro"

        binding.btnSalir.setOnClickListener {
            if (esInvitado) {
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                sesion.cerrarSesion()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        binding.switchTema.setOnCheckedChangeListener { _, activo ->
            ThemeManager.establecerModo(this, activo)
            binding.txtTema.text = if (activo) "Oscuro" else "Claro"
            Toast.makeText(
                this,
                if (activo) "Modo oscuro activado" else "Modo claro activado",
                Toast.LENGTH_SHORT
            ).show()
        }

        adapter = RecetaAdapter(db.listarRecetas()) { receta ->
            abrirDetalle(receta.id)
        }
        binding.recyclerSugeridas.layoutManager = LinearLayoutManager(this)
        binding.recyclerSugeridas.adapter = adapter

        binding.btnBuscar.setOnClickListener {
            val texto = binding.edtIngredientes.text.toString()
            val ingredientes = texto.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val intent = Intent(this, ResultadosActivity::class.java)
            intent.putExtra("ingredientes", ingredientes.toTypedArray())
            // Enviamos el estado real del filtro elegido por el usuario (RF-05)
            intent.putExtra("economicas", binding.checkEconomicas.isChecked)
            startActivity(intent)
        }

        binding.navHome.setOnClickListener { adapter.actualizar(db.listarRecetas()) }
        binding.navFeed.setOnClickListener { startActivity(Intent(this, FeedActivity::class.java)) }

        binding.navFavoritos.setOnClickListener {
            if (!sesion.haySesion()) {
                LoginRapido.mostrar(this, "Entra a tu cuenta para ver tus favoritos") {
                    actualizarCabecera()
                    startActivity(Intent(this, FavoritosActivity::class.java))
                }
                return@setOnClickListener
            }
            startActivity(Intent(this, FavoritosActivity::class.java))
        }

        binding.navCarrito.setOnClickListener {
            if (!sesion.haySesion()) {
                LoginRapido.mostrar(this, "Entra a tu cuenta para ver tu lista de compras") {
                    actualizarCabecera()
                    startActivity(Intent(this, ComprasActivity::class.java))
                }
                return@setOnClickListener
            }
            startActivity(Intent(this, ComprasActivity::class.java))
        }

        binding.contenedorFotoPerfil.setOnClickListener {
            startActivity(Intent(this, PerfilActivity::class.java))
        }

        binding.navCrear.setOnClickListener {
            if (!sesion.haySesion()) {
                LoginRapido.mostrar(this, "Entra a tu cuenta para crear una receta") {
                    actualizarCabecera()
                    startActivity(Intent(this, CrearRecetaActivity::class.java))
                }
                return@setOnClickListener
            }
            startActivity(Intent(this, CrearRecetaActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        actualizarCabecera()
        adapter.actualizar(db.listarRecetas())
    }

    // El invitado puede ver la lista, pero para abrir el detalle debe iniciar sesion.
    // Mostramos la mini pantalla de login y, al entrar, abrimos la receta que toco.
    private fun abrirDetalle(recetaId: Int) {
        if (!sesion.haySesion()) {
            LoginRapido.mostrar(this, "Entra a tu cuenta para ver esta receta") {
                actualizarCabecera()
                verDetalle(recetaId)
            }
            return
        }
        verDetalle(recetaId)
    }

    private fun verDetalle(recetaId: Int) {
        val intent = Intent(this, DetalleRecetaActivity::class.java)
        intent.putExtra("recetaId", recetaId)
        startActivity(intent)
    }

    // Refresca el saludo y el boton al cambiar el estado de la sesion
    private fun actualizarCabecera() {
        esInvitado = !sesion.haySesion()
        binding.txtSaludo.text = if (esInvitado) "Hola, Invitado" else "Hola, ${sesion.getNombre()}"
        binding.btnSalir.text = if (esInvitado) "Entrar" else "Salir"
        // El invitado aun no tiene cuenta, asi que no mostramos avatar hasta que inicie sesion
        binding.contenedorFotoPerfil.visibility = if (esInvitado) View.GONE else View.VISIBLE
        if (!esInvitado) mostrarAvatar()
    }

    // Muestra la foto solo si la cuenta tiene una guardada. Las demas cuentas
    // (incluidas las recien creadas) muestran la inicial de su nombre.
    private fun mostrarAvatar() {
        val usuario = db.obtenerUsuario(sesion.getUsuarioId())
        val foto = usuario?.foto ?: ""
        val nombre = usuario?.nombre ?: sesion.getNombre()
        // Solo hay foto si la cuenta tiene una guardada y ademas se pudo cargar
        val hayFoto = foto.isNotEmpty() &&
            ImagenUtil.mostrar(binding.imgFotoPerfil, foto, redondeado = true)
        binding.imgFotoPerfil.visibility = if (hayFoto) View.VISIBLE else View.GONE
        binding.txtInicialPerfil.visibility = if (hayFoto) View.GONE else View.VISIBLE
        if (!hayFoto) binding.txtInicialPerfil.text = nombre.firstOrNull()?.uppercase() ?: "?"
    }
}
