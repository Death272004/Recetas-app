package com.utp.recetaslid.model

data class Usuario(
    val id: Int,
    val nombre: String,
    val correo: String,
    val clave: String,
    val rol: String,
    val estado: String = "activo",
    val fechaRegistro: String = "",
    val ultimoAcceso: String = "",
    // Nombre del drawable o ruta de la foto de perfil. Vacio = sin foto (se usa la inicial)
    val foto: String = ""
)
