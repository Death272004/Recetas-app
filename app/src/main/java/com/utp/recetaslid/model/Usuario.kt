package com.utp.recetaslid.model

data class Usuario(
    val id: Int,
    val nombre: String,
    val correo: String,
    val clave: String,
    val rol: String,
    val estado: String = "activo",
    val fechaRegistro: String = "",
    val ultimoAcceso: String = ""
)
