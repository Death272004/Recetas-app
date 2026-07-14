package com.utp.recetaslid.model

data class Comentario(
    val id: Int,
    val usuarioId: Int,
    val recetaId: Int,
    val texto: String,
    val fecha: String,
    val nombreUsuario: String
)
