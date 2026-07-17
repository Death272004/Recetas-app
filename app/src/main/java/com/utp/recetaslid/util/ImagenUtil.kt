package com.utp.recetaslid.util

import android.net.Uri
import android.widget.ImageView
import com.utp.recetaslid.R
import java.io.File

// Resuelve y muestra la imagen de una receta en un ImageView.
// El campo Receta.imagen puede ser el nombre de un drawable (recetas precargadas del sistema)
// o la ruta absoluta de un archivo guardado al crear/editar una receta con foto propia.
object ImagenUtil {

    // Devuelve true si se mostro una foto real, o false si se mostro el icono generico
    fun mostrar(
        imageView: ImageView,
        imagen: String,
        redondeado: Boolean = false,
        fondoVacio: Int = R.color.naranja_suave,
        paddingVacioDp: Int = 10
    ): Boolean {
        val ctx = imageView.context
        val archivo = imagen.takeIf { it.startsWith("/") }?.let { File(it) }?.takeIf { it.exists() }
        val resId = if (archivo == null && imagen.isNotEmpty())
            ctx.resources.getIdentifier(imagen, "drawable", ctx.packageName) else 0

        if (archivo != null || resId != 0) {
            if (archivo != null) imageView.setImageURI(Uri.fromFile(archivo))
            else imageView.setImageResource(resId)
            imageView.setPadding(0, 0, 0, 0)
            // Con bordes redondeados dejamos el fondo tal cual: define la forma que usa
            // clipToOutline para recortar la foto. Sin ellos, lo limpiamos porque la foto
            // ya cubre todo el cuadro.
            if (!redondeado) imageView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            return true
        }

        val pad = (paddingVacioDp * ctx.resources.displayMetrics.density).toInt()
        imageView.setImageResource(R.drawable.ic_receta)
        imageView.setPadding(pad, pad, pad, pad)
        imageView.setBackgroundResource(fondoVacio)
        return false
    }
}
