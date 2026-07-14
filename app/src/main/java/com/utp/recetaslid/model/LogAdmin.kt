package com.utp.recetaslid.model

data class LogAdmin(
    val id: Int,
    val adminId: Int,
    val accion: String,
    val detalle: String,
    val fecha: String,
    val nombreAdmin: String = ""
)
