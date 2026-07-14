package com.utp.recetaslid.model

// Modelo que representa a un usuario de la aplicacion
data class Usuario(
    val id: Int,
    val nombre: String,
    val correo: String,
    val clave: String,
    val rol: String   // "usuario" o "admin"
)
