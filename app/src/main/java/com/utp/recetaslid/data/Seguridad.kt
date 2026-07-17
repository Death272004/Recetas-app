package com.utp.recetaslid.data

import java.security.MessageDigest

// Utilidad de seguridad para proteger las contraseñas (RNF-03).
// Las claves NO se guardan en texto plano: se almacena su resumen (hash) SHA-256.
// Un hash es de una sola via: se puede calcular a partir de la clave,
// pero no se puede volver a obtener la clave original a partir del hash.
object Seguridad {

    // Valor fijo que se agrega a la clave antes de calcular el hash.
    // Sirve para que dos usuarios con la misma clave no tengan el mismo hash.
    private const val SAL = "RecetasLID2026"

    // Convierte una clave en su hash SHA-256 representado en hexadecimal
    fun hashClave(clave: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest((SAL + clave).toByteArray(Charsets.UTF_8))
        // Pasamos cada byte a dos caracteres hexadecimales
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

    // Compara una clave escrita por el usuario contra el hash guardado
    fun verificar(claveIngresada: String, hashGuardado: String): Boolean {
        return hashClave(claveIngresada) == hashGuardado
    }
}
