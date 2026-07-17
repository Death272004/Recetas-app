package com.utp.recetaslid.data

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

// Carga los datos iniciales: cuenta admin y recetas precargadas
object SeedData {

    fun cargar(db: SQLiteDatabase) {
        // Cuenta de administrador por defecto
        // El administrador no tiene foto: se mostrara la inicial de su nombre
        insertarUsuario(db, "Administrador", "admin", "Admin123", "admin",
            "¿Nombre de tu primera mascota?", "firulais")
        // Un usuario de ejemplo para pruebas
        // Unica cuenta con foto de perfil propia. Las demas cuentas usan su inicial.
        insertarUsuario(db, "Leonardo", "leo@correo.com", "123456", "usuario",
            "¿Nombre de tu primera mascota?", "firulais", "foto_perfil_leo")

        // Recetas precargadas del sistema (autorId = 0)
        insertarReceta(db, "Arroz con pollo", "pollo, arroz, cebolla",
            "1. Sofrie la cebolla y el pollo.\n2. Agrega el arroz y agua.\n3. Cocina 20 minutos.",
            25, 1.80, "arroz_con_pollo", "https://www.youtube.com/results?search_query=receta+arroz+con+pollo")
        insertarReceta(db, "Tortilla de huevo", "huevo, sal",
            "1. Bate los huevos con sal.\n2. Cocina en sarten hasta dorar.",
            10, 0.90, "tortilla_de_huevo", "https://www.youtube.com/results?search_query=receta+tortilla+de+huevo")
        insertarReceta(db, "Sopa de pollo", "pollo, fideos, cebolla, sal",
            "1. Hierve el pollo.\n2. Agrega fideos y cebolla.\n3. Sazona al gusto.",
            30, 2.40, "sopa_de_pollo", "https://www.youtube.com/results?search_query=receta+sopa+de+pollo")
        insertarReceta(db, "Arroz frito", "arroz, huevo, salsa de soya",
            "1. Sofrie el arroz cocido.\n2. Agrega huevo y salsa de soya.",
            15, 1.20, "arroz_frito", "https://www.youtube.com/watch?v=tromyyuefTs")
        insertarReceta(db, "Ensalada simple", "lechuga, tomate, sal",
            "1. Pica los vegetales.\n2. Mezcla y agrega sal y limon.",
            8, 1.10, "ensalada_simple", "https://www.youtube.com/watch?v=UYyHci_ZzxY")
        insertarReceta(db, "Pollo guisado", "pollo, tomate, cebolla, ajo, sal",
            "1. Sofrie ajo y cebolla.\n2. Agrega pollo y tomate.\n3. Guisa 25 minutos.",
            35, 3.10, "pollo_guisado", "https://www.youtube.com/watch?v=fJoLYnYJOy0")
        insertarReceta(db, "Panqueques", "huevo, harina, leche, azucar",
            "1. Mezcla harina, huevo, leche y azucar.\n2. Vierte en sarten caliente.\n3. Cocina hasta dorar por ambos lados.",
            15, 1.50, "panqueques", "https://www.youtube.com/results?search_query=receta+panqueques+faciles")
        insertarReceta(db, "Ceviche de camarones", "camarones, limon, cebolla, cilantro, sal",
            "1. Cocina los camarones.\n2. Pica cebolla y cilantro.\n3. Mezcla todo con jugo de limon y sal.\n4. Refrigera 15 minutos.",
            20, 4.50, "ceviche_de_camarones", "https://www.youtube.com/results?search_query=receta+ceviche+de+camarones")
        insertarReceta(db, "Pasta con salsa de tomate", "pasta, tomate, ajo, aceite, sal",
            "1. Hierve la pasta.\n2. Sofrie ajo en aceite.\n3. Agrega tomate picado y sal.\n4. Mezcla con la pasta.",
            20, 2.00, "pasta_salsa_tomate", "https://www.youtube.com/results?search_query=receta+pasta+salsa+tomate")
        insertarReceta(db, "Sandwich de pollo", "pan, pollo, lechuga, tomate, mayonesa",
            "1. Cocina y desmenuza el pollo.\n2. Mezcla con mayonesa.\n3. Arma el sandwich con lechuga y tomate.",
            10, 2.50, "sandwich_de_pollo", "https://www.youtube.com/results?search_query=receta+sandwich+de+pollo")
    }

    private fun insertarUsuario(
        db: SQLiteDatabase, nombre: String, correo: String, clave: String, rol: String,
        pregunta: String = "", respuesta: String = "", foto: String = ""
    ) {
        val v = ContentValues().apply {
            put("nombre", nombre)
            put("correo", correo)
            // La clave se guarda como hash, igual que en el registro (RNF-03)
            put("clave", Seguridad.hashClave(clave))
            put("rol", rol)
            put("estado", "activo")
            put("fechaRegistro", System.currentTimeMillis().toString())
            put("pregunta", pregunta)
            put("respuesta", if (respuesta.isEmpty()) "" else Seguridad.hashClave(respuesta.trim().lowercase()))
            put("foto", foto)
        }
        db.insert("usuarios", null, v)
    }

    private fun insertarReceta(
        db: SQLiteDatabase, titulo: String, ingredientes: String, pasos: String,
        tiempo: Int, costo: Double, imagen: String, videoUrl: String
    ) {
        val v = ContentValues().apply {
            put("titulo", titulo)
            put("ingredientes", ingredientes)
            put("pasos", pasos)
            put("tiempo", tiempo)
            put("costo", costo)
            put("autorId", 0)
            put("reportada", 0)
            put("imagen", imagen)
            put("videoUrl", videoUrl)
        }
        db.insert("recetas", null, v)
    }
}
