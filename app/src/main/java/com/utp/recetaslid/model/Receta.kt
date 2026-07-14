package com.utp.recetaslid.model

// Modelo que representa una receta
data class Receta(
    val id: Int,
    val titulo: String,
    val ingredientes: String,   // ingredientes separados por coma
    val pasos: String,
    val tiempo: Int,            // tiempo en minutos
    val costo: Double,          // costo estimado en B/.
    val autorId: Int,          // 0 = receta precargada del sistema
    val reportada: Boolean = false,
    val imagen: String = "",    // nombre del drawable sin extension
    val videoUrl: String = "",
    val oculta: Boolean = false,
    val destacada: Boolean = false
) {
    // Devuelve la lista de ingredientes ya separada y en minuscula
    fun listaIngredientes(): List<String> =
        ingredientes.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
}
