package com.utp.recetaslid.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.utp.recetaslid.R
import com.utp.recetaslid.data.DBHelper
import com.utp.recetaslid.data.SessionManager
import com.utp.recetaslid.databinding.ActivityCrearRecetaBinding
import com.utp.recetaslid.model.Receta
import com.utp.recetaslid.util.ImagenUtil
import java.io.File
import java.io.FileOutputStream

// Pantalla del formulario de recetas.
// Sirve para crear una receta nueva y tambien para editar una existente (RF-07).
// Si se recibe el extra "recetaId", la pantalla entra en modo edicion.
class CrearRecetaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrearRecetaBinding
    private lateinit var db: DBHelper
    private lateinit var sesion: SessionManager

    // -1 significa que estamos creando una receta nueva
    private var recetaId = -1
    private var modoEdicion = false

    // Ruta del archivo de la foto adjuntada, o el nombre del drawable si venimos de editar
    // una receta que ya tenia imagen. Vacio si la receta no tiene foto.
    private var imagenActual = ""

    private val selectorImagen = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) procesarImagenSeleccionada(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearRecetaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.imgReceta.clipToOutline = true

        db = DBHelper(this)
        sesion = SessionManager(this)

        recetaId = intent.getIntExtra("recetaId", -1)
        modoEdicion = recetaId != -1

        if (modoEdicion) prepararEdicion()

        binding.btnVolver.setOnClickListener { finish() }
        binding.btnGuardar.setOnClickListener { guardar() }
        binding.btnAdjuntarImagen.setOnClickListener { selectorImagen.launch("image/*") }
    }

    // Carga los datos de la receta en el formulario y ajusta los textos
    private fun prepararEdicion() {
        val receta = db.obtenerReceta(recetaId)
        if (receta == null) {
            Toast.makeText(this, "Receta no encontrada", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Solo el autor puede editar su propia receta
        if (receta.autorId != sesion.getUsuarioId()) {
            Toast.makeText(this, "Solo puedes editar tus propias recetas", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        binding.txtTituloPantalla.text = "Editar receta"
        binding.btnGuardar.text = "Guardar cambios"

        binding.edtTitulo.setText(receta.titulo)
        binding.edtIngredientes.setText(receta.ingredientes)
        binding.edtTiempo.setText(receta.tiempo.toString())
        binding.edtCosto.setText(receta.costo.toString())
        binding.edtPasos.setText(receta.pasos)
        binding.edtVideoUrl.setText(receta.videoUrl)

        imagenActual = receta.imagen
        ImagenUtil.mostrar(
            binding.imgReceta, imagenActual,
            redondeado = true, fondoVacio = R.drawable.bg_imagen_receta, paddingVacioDp = 24
        )
    }

    // Recorta la foto elegida al centro (cuadrada), la reduce a 240x240 y la guarda en el
    // almacenamiento interno de la app, igual que el formato de las recetas precargadas.
    private fun procesarImagenSeleccionada(uri: Uri) {
        try {
            val opcionesLimites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opcionesLimites)
            }
            var muestreo = 1
            val ladoMayor = maxOf(opcionesLimites.outWidth, opcionesLimites.outHeight)
            while (ladoMayor / muestreo > 1000) muestreo *= 2

            val opciones = BitmapFactory.Options().apply { inSampleSize = muestreo }
            val original = contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opciones)
            }
            if (original == null) {
                Toast.makeText(this, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show()
                return
            }

            val lado = minOf(original.width, original.height)
            val x = (original.width - lado) / 2
            val y = (original.height - lado) / 2
            val cuadrada = Bitmap.createBitmap(original, x, y, lado, lado)
            val final = Bitmap.createScaledBitmap(cuadrada, 240, 240, true)

            val carpeta = File(filesDir, "recetas").apply { mkdirs() }
            val archivo = File(carpeta, "receta_${System.currentTimeMillis()}.jpg")
            FileOutputStream(archivo).use { salida ->
                final.compress(Bitmap.CompressFormat.JPEG, 90, salida)
            }

            imagenActual = archivo.absolutePath
            ImagenUtil.mostrar(
                binding.imgReceta, imagenActual,
                redondeado = true, fondoVacio = R.drawable.bg_imagen_receta, paddingVacioDp = 24
            )
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardar() {
        val titulo = binding.edtTitulo.text.toString().trim()
        val ingredientes = binding.edtIngredientes.text.toString().trim()
        val tiempoTexto = binding.edtTiempo.text.toString().trim()
        val costoTexto = binding.edtCosto.text.toString().trim()
        val pasos = binding.edtPasos.text.toString().trim()
        val videoUrl = binding.edtVideoUrl.text.toString().trim()

        if (titulo.isEmpty() || ingredientes.isEmpty() || tiempoTexto.isEmpty()
            || costoTexto.isEmpty() || pasos.isEmpty()
        ) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val tiempo = tiempoTexto.toIntOrNull()
        val costo = costoTexto.toDoubleOrNull()
        if (tiempo == null || costo == null) {
            Toast.makeText(this, "Tiempo y costo deben ser numeros", Toast.LENGTH_SHORT).show()
            return
        }

        val receta = Receta(
            id = if (modoEdicion) recetaId else 0,
            titulo = titulo,
            ingredientes = ingredientes,
            pasos = pasos,
            tiempo = tiempo,
            costo = costo,
            autorId = sesion.getUsuarioId(),
            imagen = imagenActual,
            videoUrl = videoUrl
        )

        if (modoEdicion) {
            db.actualizarReceta(receta)
            Toast.makeText(this, "Receta actualizada", Toast.LENGTH_SHORT).show()
        } else {
            db.insertarReceta(receta)
            Toast.makeText(this, "Receta guardada", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}
