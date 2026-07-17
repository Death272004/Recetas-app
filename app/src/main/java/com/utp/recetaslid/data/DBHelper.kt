package com.utp.recetaslid.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.utp.recetaslid.model.Comentario
import com.utp.recetaslid.model.LogAdmin
import com.utp.recetaslid.model.Receta
import com.utp.recetaslid.model.Usuario

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, "recetas_lid.db", null, 7) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT, correo TEXT UNIQUE, clave TEXT, rol TEXT, " +
                "estado TEXT DEFAULT 'activo', " +
                "fechaRegistro TEXT DEFAULT '', " +
                "ultimoAcceso TEXT DEFAULT '', " +
                "pregunta TEXT DEFAULT '', " +
                "respuesta TEXT DEFAULT '', " +
                "foto TEXT DEFAULT '')"
        )
        db.execSQL(
            "CREATE TABLE recetas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "titulo TEXT, ingredientes TEXT, pasos TEXT, " +
                "tiempo INTEGER, costo REAL, autorId INTEGER, reportada INTEGER DEFAULT 0, " +
                "imagen TEXT, videoUrl TEXT, " +
                "oculta INTEGER DEFAULT 0, destacada INTEGER DEFAULT 0)"
        )
        db.execSQL(
            "CREATE TABLE favoritos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, usuarioId INTEGER, recetaId INTEGER)"
        )
        db.execSQL(
            "CREATE TABLE compras (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, usuarioId INTEGER, " +
                "item TEXT, precio REAL, comprado INTEGER DEFAULT 0)"
        )
        db.execSQL(
            "CREATE TABLE comentarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, usuarioId INTEGER, " +
                "recetaId INTEGER, texto TEXT, fecha TEXT, " +
                "estado TEXT DEFAULT 'aprobado')"
        )
        db.execSQL(
            "CREATE TABLE logs_admin (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "adminId INTEGER, accion TEXT, detalle TEXT, fecha TEXT)"
        )
        db.execSQL(
            "CREATE TABLE papelera (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "tipo TEXT, contenido TEXT, fechaEliminacion TEXT, eliminadoPor INTEGER)"
        )
        SeedData.cargar(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS usuarios")
        db.execSQL("DROP TABLE IF EXISTS recetas")
        db.execSQL("DROP TABLE IF EXISTS favoritos")
        db.execSQL("DROP TABLE IF EXISTS compras")
        db.execSQL("DROP TABLE IF EXISTS comentarios")
        db.execSQL("DROP TABLE IF EXISTS logs_admin")
        db.execSQL("DROP TABLE IF EXISTS papelera")
        onCreate(db)
    }

    // ---------------- USUARIOS ----------------

    fun registrarUsuario(
        nombre: String, correo: String, clave: String,
        pregunta: String = "", respuesta: String = ""
    ): Boolean {
        if (existeCorreo(correo)) return false
        val valores = ContentValues().apply {
            put("nombre", nombre)
            put("correo", correo)
            // Guardamos el hash de la clave, nunca la clave en texto plano (RNF-03)
            put("clave", Seguridad.hashClave(clave))
            put("rol", "usuario")
            put("estado", "activo")
            put("fechaRegistro", System.currentTimeMillis().toString())
            put("pregunta", pregunta)
            // La respuesta tambien se guarda como hash (normalizada a minusculas)
            put("respuesta", if (respuesta.isEmpty()) "" else Seguridad.hashClave(respuesta.trim().lowercase()))
        }
        val resultado = writableDatabase.insert("usuarios", null, valores)
        return resultado != -1L
    }

    fun existeCorreo(correo: String): Boolean {
        val c = readableDatabase.rawQuery(
            "SELECT id FROM usuarios WHERE correo = ?", arrayOf(correo)
        )
        val existe = c.count > 0
        c.close()
        return existe
    }

    fun login(correo: String, clave: String): Usuario? {
        // Comparamos el hash de lo que escribio el usuario contra el hash guardado
        val c = readableDatabase.rawQuery(
            "SELECT id, nombre, correo, clave, rol, estado FROM usuarios WHERE correo = ? AND clave = ?",
            arrayOf(correo, Seguridad.hashClave(clave))
        )
        var usuario: Usuario? = null
        if (c.moveToFirst()) {
            val estado = c.getString(5) ?: "activo"
            if (estado != "bloqueado" && estado != "suspendido") {
                usuario = Usuario(
                    c.getInt(0), c.getString(1), c.getString(2),
                    c.getString(3), c.getString(4), estado
                )
                actualizarUltimoAcceso(usuario.id)
            }
        }
        c.close()
        return usuario
    }

    fun listarUsuarios(): List<Usuario> {
        val lista = mutableListOf<Usuario>()
        val c = readableDatabase.rawQuery(
            "SELECT id, nombre, correo, clave, rol FROM usuarios ORDER BY id", null
        )
        while (c.moveToNext()) {
            lista.add(
                Usuario(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4))
            )
        }
        c.close()
        return lista
    }

    fun eliminarUsuario(id: Int) {
        writableDatabase.delete("usuarios", "id = ? AND rol != 'admin'", arrayOf(id.toString()))
    }

    fun contarUsuarios(): Int = contar("usuarios")

    // ---------------- RECETAS ----------------

    fun insertarReceta(r: Receta): Long {
        val valores = ContentValues().apply {
            put("titulo", r.titulo)
            put("ingredientes", r.ingredientes)
            put("pasos", r.pasos)
            put("tiempo", r.tiempo)
            put("costo", r.costo)
            put("autorId", r.autorId)
            put("reportada", if (r.reportada) 1 else 0)
            put("imagen", r.imagen)
            put("videoUrl", r.videoUrl)
        }
        return writableDatabase.insert("recetas", null, valores)
    }

    fun listarRecetas(): List<Receta> {
        val lista = mutableListOf<Receta>()
        val c = readableDatabase.rawQuery(
            "SELECT * FROM recetas WHERE oculta = 0 ORDER BY destacada DESC, id DESC", null
        )
        while (c.moveToNext()) lista.add(cursorAReceta(c))
        c.close()
        return lista
    }

    fun obtenerReceta(id: Int): Receta? {
        val c = readableDatabase.rawQuery("SELECT * FROM recetas WHERE id = ?", arrayOf(id.toString()))
        var r: Receta? = null
        if (c.moveToFirst()) r = cursorAReceta(c)
        c.close()
        return r
    }

    // Actualiza una receta existente (RF-07: editar receta propia)
    fun actualizarReceta(r: Receta): Int {
        val valores = ContentValues().apply {
            put("titulo", r.titulo)
            put("ingredientes", r.ingredientes)
            put("pasos", r.pasos)
            put("tiempo", r.tiempo)
            put("costo", r.costo)
            put("videoUrl", r.videoUrl)
            put("imagen", r.imagen)
        }
        return writableDatabase.update("recetas", valores, "id = ?", arrayOf(r.id.toString()))
    }

    fun eliminarReceta(id: Int) {
        writableDatabase.delete("recetas", "id = ?", arrayOf(id.toString()))
        writableDatabase.delete("favoritos", "recetaId = ?", arrayOf(id.toString()))
    }

    fun reportarReceta(id: Int) {
        val v = ContentValues().apply { put("reportada", 1) }
        writableDatabase.update("recetas", v, "id = ?", arrayOf(id.toString()))
    }

    fun listarReportadas(): List<Receta> {
        val lista = mutableListOf<Receta>()
        val c = readableDatabase.rawQuery("SELECT * FROM recetas WHERE reportada = 1", null)
        while (c.moveToNext()) lista.add(cursorAReceta(c))
        c.close()
        return lista
    }

    fun contarRecetas(): Int = contar("recetas")

    fun buscarPorIngredientes(ingredientes: List<String>, soloEconomicas: Boolean): List<Receta> {
        val resultado = mutableListOf<Receta>()
        val buscados = ingredientes.map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        for (r in listarRecetas()) {
            val deLaReceta = r.listaIngredientes()
            val coincide = buscados.isEmpty() ||
                buscados.any { b -> deLaReceta.any { it.contains(b) } }
            if (!coincide) continue
            if (soloEconomicas && !(r.costo <= 2.0 || deLaReceta.size <= 3)) continue
            resultado.add(r)
        }
        return resultado.sortedWith(
            compareBy({ faltantes(it, buscados).size }, { it.costo })
        )
    }

    fun faltantes(receta: Receta, disponibles: List<String>): List<String> {
        val disp = disponibles.map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        return receta.listaIngredientes().filter { ing -> disp.none { ing.contains(it) || it.contains(ing) } }
    }

    private fun cursorAReceta(c: android.database.Cursor): Receta {
        return Receta(
            c.getInt(c.getColumnIndexOrThrow("id")),
            c.getString(c.getColumnIndexOrThrow("titulo")),
            c.getString(c.getColumnIndexOrThrow("ingredientes")),
            c.getString(c.getColumnIndexOrThrow("pasos")),
            c.getInt(c.getColumnIndexOrThrow("tiempo")),
            c.getDouble(c.getColumnIndexOrThrow("costo")),
            c.getInt(c.getColumnIndexOrThrow("autorId")),
            c.getInt(c.getColumnIndexOrThrow("reportada")) == 1,
            c.getString(c.getColumnIndexOrThrow("imagen")) ?: "",
            c.getString(c.getColumnIndexOrThrow("videoUrl")) ?: "",
            c.getInt(c.getColumnIndexOrThrow("oculta")) == 1,
            c.getInt(c.getColumnIndexOrThrow("destacada")) == 1
        )
    }

    // ---------------- FAVORITOS ----------------

    fun esFavorito(usuarioId: Int, recetaId: Int): Boolean {
        val c = readableDatabase.rawQuery(
            "SELECT id FROM favoritos WHERE usuarioId = ? AND recetaId = ?",
            arrayOf(usuarioId.toString(), recetaId.toString())
        )
        val res = c.count > 0
        c.close()
        return res
    }

    fun alternarFavorito(usuarioId: Int, recetaId: Int): Boolean {
        return if (esFavorito(usuarioId, recetaId)) {
            writableDatabase.delete(
                "favoritos", "usuarioId = ? AND recetaId = ?",
                arrayOf(usuarioId.toString(), recetaId.toString())
            )
            false
        } else {
            val v = ContentValues().apply {
                put("usuarioId", usuarioId)
                put("recetaId", recetaId)
            }
            writableDatabase.insert("favoritos", null, v)
            true
        }
    }

    fun listarFavoritos(usuarioId: Int): List<Receta> {
        val lista = mutableListOf<Receta>()
        val c = readableDatabase.rawQuery(
            "SELECT r.* FROM recetas r INNER JOIN favoritos f ON r.id = f.recetaId " +
                "WHERE f.usuarioId = ?", arrayOf(usuarioId.toString())
        )
        while (c.moveToNext()) lista.add(cursorAReceta(c))
        c.close()
        return lista
    }

    // ---------------- LISTA DE COMPRAS ----------------

    fun agregarACompras(usuarioId: Int, item: String, precio: Double) {
        val c = readableDatabase.rawQuery(
            "SELECT id FROM compras WHERE usuarioId = ? AND item = ?",
            arrayOf(usuarioId.toString(), item)
        )
        val yaExiste = c.count > 0
        c.close()
        if (yaExiste) return
        val v = ContentValues().apply {
            put("usuarioId", usuarioId)
            put("item", item)
            put("precio", precio)
            put("comprado", 0)
        }
        writableDatabase.insert("compras", null, v)
    }

    fun listarCompras(usuarioId: Int): List<ItemCompra> {
        val lista = mutableListOf<ItemCompra>()
        val c = readableDatabase.rawQuery(
            "SELECT id, item, precio, comprado FROM compras WHERE usuarioId = ?",
            arrayOf(usuarioId.toString())
        )
        while (c.moveToNext()) {
            lista.add(ItemCompra(c.getInt(0), c.getString(1), c.getDouble(2), c.getInt(3) == 1))
        }
        c.close()
        return lista
    }

    fun marcarComprado(id: Int, comprado: Boolean) {
        val v = ContentValues().apply { put("comprado", if (comprado) 1 else 0) }
        writableDatabase.update("compras", v, "id = ?", arrayOf(id.toString()))
    }

    fun eliminarCompra(id: Int) {
        writableDatabase.delete("compras", "id = ?", arrayOf(id.toString()))
    }

    // Marca todos los items de la lista de compras del usuario como comprados
    fun marcarTodoComprado(usuarioId: Int) {
        val v = ContentValues().apply { put("comprado", 1) }
        writableDatabase.update("compras", v, "usuarioId = ?", arrayOf(usuarioId.toString()))
    }

    // Vacia por completo la lista de compras del usuario
    fun limpiarCompras(usuarioId: Int) {
        writableDatabase.delete("compras", "usuarioId = ?", arrayOf(usuarioId.toString()))
    }

    // ---------------- PERFIL / ESTADISTICAS ----------------

    fun obtenerUsuario(id: Int): Usuario? {
        val c = readableDatabase.rawQuery(
            "SELECT id, nombre, correo, clave, rol, foto FROM usuarios WHERE id = ?",
            arrayOf(id.toString())
        )
        var u: Usuario? = null
        if (c.moveToFirst()) {
            u = Usuario(
                c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4),
                foto = c.getString(5) ?: ""
            )
        }
        c.close()
        return u
    }

    fun actualizarNombre(id: Int, nombre: String) {
        val v = ContentValues().apply { put("nombre", nombre) }
        writableDatabase.update("usuarios", v, "id = ?", arrayOf(id.toString()))
    }

    fun actualizarFotoPerfil(id: Int, foto: String) {
        val v = ContentValues().apply { put("foto", foto) }
        writableDatabase.update("usuarios", v, "id = ?", arrayOf(id.toString()))
    }

    fun listarUsuariosQueDieronLike(recetaId: Int): List<Usuario> {
        val lista = mutableListOf<Usuario>()
        val c = readableDatabase.rawQuery(
            "SELECT u.id, u.nombre, u.correo, u.clave, u.rol, u.estado, " +
                "u.fechaRegistro, u.ultimoAcceso, u.foto " +
                "FROM favoritos f INNER JOIN usuarios u ON f.usuarioId = u.id " +
                "WHERE f.recetaId = ? ORDER BY u.nombre COLLATE NOCASE",
            arrayOf(recetaId.toString())
        )
        while (c.moveToNext()) {
            lista.add(
                Usuario(
                    c.getInt(0), c.getString(1), c.getString(2), c.getString(3),
                    c.getString(4), c.getString(5) ?: "activo",
                    c.getString(6) ?: "", c.getString(7) ?: "",
                    c.getString(8) ?: ""
                )
            )
        }
        c.close()
        return lista
    }

    fun contarRecetasDeUsuario(autorId: Int): Int {
        val c = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM recetas WHERE autorId = ?", arrayOf(autorId.toString())
        )
        c.moveToFirst(); val t = c.getInt(0); c.close(); return t
    }

    fun listarRecetasDeUsuario(autorId: Int): List<Receta> {
        val lista = mutableListOf<Receta>()
        val c = readableDatabase.rawQuery(
            "SELECT * FROM recetas WHERE autorId = ? ORDER BY id DESC", arrayOf(autorId.toString())
        )
        while (c.moveToNext()) lista.add(cursorAReceta(c))
        c.close()
        return lista
    }

    fun contarFavoritosDeUsuario(usuarioId: Int): Int {
        val c = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM favoritos WHERE usuarioId = ?", arrayOf(usuarioId.toString())
        )
        c.moveToFirst(); val t = c.getInt(0); c.close(); return t
    }

    fun contarLikesReceta(recetaId: Int): Int {
        val c = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM favoritos WHERE recetaId = ?", arrayOf(recetaId.toString())
        )
        c.moveToFirst(); val t = c.getInt(0); c.close(); return t
    }

    fun contarComprasDeUsuario(usuarioId: Int): Int {
        val c = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM compras WHERE usuarioId = ?", arrayOf(usuarioId.toString())
        )
        c.moveToFirst(); val t = c.getInt(0); c.close(); return t
    }

    fun buscarRecetas(query: String): List<Receta> {
        val lista = mutableListOf<Receta>()
        val c = readableDatabase.rawQuery(
            "SELECT * FROM recetas WHERE oculta = 0 AND titulo LIKE ? ORDER BY id DESC",
            arrayOf("%$query%")
        )
        while (c.moveToNext()) lista.add(cursorAReceta(c))
        c.close()
        return lista
    }

    // ---------------- COMENTARIOS ----------------

    fun insertarComentario(usuarioId: Int, recetaId: Int, texto: String) {
        val v = ContentValues().apply {
            put("usuarioId", usuarioId)
            put("recetaId", recetaId)
            put("texto", texto)
            put("fecha", System.currentTimeMillis().toString())
        }
        writableDatabase.insert("comentarios", null, v)
    }

    fun listarComentarios(recetaId: Int): List<Comentario> {
        val lista = mutableListOf<Comentario>()
        val c = readableDatabase.rawQuery(
            "SELECT c.id, c.usuarioId, c.recetaId, c.texto, c.fecha, u.nombre " +
                "FROM comentarios c LEFT JOIN usuarios u ON c.usuarioId = u.id " +
                "WHERE c.recetaId = ? AND c.estado = 'aprobado' ORDER BY c.id DESC",
            arrayOf(recetaId.toString())
        )
        while (c.moveToNext()) {
            lista.add(
                Comentario(
                    c.getInt(0), c.getInt(1), c.getInt(2),
                    c.getString(3), c.getString(4),
                    c.getString(5) ?: "Usuario"
                )
            )
        }
        c.close()
        return lista
    }

    fun contarComentarios(recetaId: Int): Int {
        val c = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM comentarios WHERE recetaId = ?", arrayOf(recetaId.toString())
        )
        c.moveToFirst(); val t = c.getInt(0); c.close(); return t
    }

    fun eliminarComentario(id: Int) {
        writableDatabase.delete("comentarios", "id = ?", arrayOf(id.toString()))
    }

    // ---------------- ADMIN: USUARIOS ----------------

    fun listarUsuariosAdmin(): List<Usuario> {
        val lista = mutableListOf<Usuario>()
        val c = readableDatabase.rawQuery(
            "SELECT id, nombre, correo, clave, rol, estado, fechaRegistro, ultimoAcceso " +
                "FROM usuarios ORDER BY id", null
        )
        while (c.moveToNext()) {
            lista.add(
                Usuario(
                    c.getInt(0), c.getString(1), c.getString(2), c.getString(3),
                    c.getString(4), c.getString(5) ?: "activo",
                    c.getString(6) ?: "", c.getString(7) ?: ""
                )
            )
        }
        c.close()
        return lista
    }

    fun buscarUsuariosAdmin(query: String): List<Usuario> {
        val lista = mutableListOf<Usuario>()
        val c = readableDatabase.rawQuery(
            "SELECT id, nombre, correo, clave, rol, estado, fechaRegistro, ultimoAcceso " +
                "FROM usuarios WHERE nombre LIKE ? OR correo LIKE ? ORDER BY id",
            arrayOf("%$query%", "%$query%")
        )
        while (c.moveToNext()) {
            lista.add(
                Usuario(
                    c.getInt(0), c.getString(1), c.getString(2), c.getString(3),
                    c.getString(4), c.getString(5) ?: "activo",
                    c.getString(6) ?: "", c.getString(7) ?: ""
                )
            )
        }
        c.close()
        return lista
    }

    fun cambiarEstadoUsuario(id: Int, estado: String) {
        val v = ContentValues().apply { put("estado", estado) }
        writableDatabase.update("usuarios", v, "id = ?", arrayOf(id.toString()))
    }

    fun cambiarRolUsuario(id: Int, rol: String) {
        val v = ContentValues().apply { put("rol", rol) }
        writableDatabase.update("usuarios", v, "id = ?", arrayOf(id.toString()))
    }

    fun resetearClave(id: Int, nuevaClave: String) {
        val v = ContentValues().apply { put("clave", Seguridad.hashClave(nuevaClave)) }
        writableDatabase.update("usuarios", v, "id = ?", arrayOf(id.toString()))
    }

    // ---------- RECUPERACION DE CONTRASENA ----------

    // Devuelve la pregunta de seguridad asociada a un correo, o null si no aplica
    fun obtenerPreguntaPorCorreo(correo: String): String? {
        val c = readableDatabase.rawQuery(
            "SELECT pregunta FROM usuarios WHERE correo = ?", arrayOf(correo)
        )
        var pregunta: String? = null
        if (c.moveToFirst()) {
            val p = c.getString(0) ?: ""
            if (p.isNotEmpty()) pregunta = p
        }
        c.close()
        return pregunta
    }

    // Verifica la respuesta de seguridad y, si es correcta, devuelve el id del usuario
    fun verificarRespuesta(correo: String, respuesta: String): Int {
        val c = readableDatabase.rawQuery(
            "SELECT id FROM usuarios WHERE correo = ? AND respuesta = ?",
            arrayOf(correo, Seguridad.hashClave(respuesta.trim().lowercase()))
        )
        var id = -1
        if (c.moveToFirst()) id = c.getInt(0)
        c.close()
        return id
    }

    // Cambia la clave de un usuario validando primero su clave actual
    fun cambiarClave(id: Int, claveActual: String, claveNueva: String): Boolean {
        val c = readableDatabase.rawQuery(
            "SELECT id FROM usuarios WHERE id = ? AND clave = ?",
            arrayOf(id.toString(), Seguridad.hashClave(claveActual))
        )
        val coincide = c.count > 0
        c.close()
        if (!coincide) return false
        resetearClave(id, claveNueva)
        return true
    }

    fun actualizarUltimoAcceso(id: Int) {
        val v = ContentValues().apply { put("ultimoAcceso", System.currentTimeMillis().toString()) }
        writableDatabase.update("usuarios", v, "id = ?", arrayOf(id.toString()))
    }

    fun obtenerEstadoPorCredenciales(correo: String, clave: String): String? {
        val c = readableDatabase.rawQuery(
            "SELECT estado FROM usuarios WHERE correo = ? AND clave = ?",
            arrayOf(correo, Seguridad.hashClave(clave))
        )
        var estado: String? = null
        if (c.moveToFirst()) estado = c.getString(0) ?: "activo"
        c.close()
        return estado
    }

    fun contarUsuariosActivos(): Int {
        val c = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM usuarios WHERE estado = 'activo'", null
        )
        c.moveToFirst(); val t = c.getInt(0); c.close(); return t
    }

    // ---------------- ADMIN: RECETAS ----------------

    fun listarRecetasAdmin(): List<Receta> {
        val lista = mutableListOf<Receta>()
        val c = readableDatabase.rawQuery("SELECT * FROM recetas ORDER BY id DESC", null)
        while (c.moveToNext()) lista.add(cursorAReceta(c))
        c.close()
        return lista
    }

    fun buscarRecetasAdmin(query: String): List<Receta> {
        val lista = mutableListOf<Receta>()
        val c = readableDatabase.rawQuery(
            "SELECT * FROM recetas WHERE titulo LIKE ? ORDER BY id DESC", arrayOf("%$query%")
        )
        while (c.moveToNext()) lista.add(cursorAReceta(c))
        c.close()
        return lista
    }

    fun ocultarReceta(id: Int, oculta: Boolean) {
        val v = ContentValues().apply { put("oculta", if (oculta) 1 else 0) }
        writableDatabase.update("recetas", v, "id = ?", arrayOf(id.toString()))
    }

    fun destacarReceta(id: Int, destacada: Boolean) {
        val v = ContentValues().apply { put("destacada", if (destacada) 1 else 0) }
        writableDatabase.update("recetas", v, "id = ?", arrayOf(id.toString()))
    }

    fun quitarReporte(id: Int) {
        val v = ContentValues().apply { put("reportada", 0) }
        writableDatabase.update("recetas", v, "id = ?", arrayOf(id.toString()))
    }

    fun contarReportesPendientes(): Int {
        val c = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM recetas WHERE reportada = 1", null
        )
        c.moveToFirst(); val t = c.getInt(0); c.close(); return t
    }

    // ---------------- ADMIN: COMENTARIOS ----------------

    fun listarTodosComentarios(): List<Comentario> {
        val lista = mutableListOf<Comentario>()
        val c = readableDatabase.rawQuery(
            "SELECT c.id, c.usuarioId, c.recetaId, c.texto, c.fecha, u.nombre, c.estado, r.titulo " +
                "FROM comentarios c " +
                "LEFT JOIN usuarios u ON c.usuarioId = u.id " +
                "LEFT JOIN recetas r ON c.recetaId = r.id " +
                "ORDER BY c.id DESC", null
        )
        while (c.moveToNext()) {
            lista.add(
                Comentario(
                    c.getInt(0), c.getInt(1), c.getInt(2),
                    c.getString(3), c.getString(4),
                    c.getString(5) ?: "Usuario",
                    c.getString(6) ?: "aprobado",
                    c.getString(7) ?: "Receta eliminada"
                )
            )
        }
        c.close()
        return lista
    }

    fun cambiarEstadoComentario(id: Int, estado: String) {
        val v = ContentValues().apply { put("estado", estado) }
        writableDatabase.update("comentarios", v, "id = ?", arrayOf(id.toString()))
    }

    fun contarTodosComentarios(): Int = contar("comentarios")

    // ---------------- ADMIN: LOGS ----------------

    fun registrarLog(adminId: Int, accion: String, detalle: String) {
        val v = ContentValues().apply {
            put("adminId", adminId)
            put("accion", accion)
            put("detalle", detalle)
            put("fecha", System.currentTimeMillis().toString())
        }
        writableDatabase.insert("logs_admin", null, v)
    }

    fun listarLogs(): List<LogAdmin> {
        val lista = mutableListOf<LogAdmin>()
        val c = readableDatabase.rawQuery(
            "SELECT l.id, l.adminId, l.accion, l.detalle, l.fecha, u.nombre " +
                "FROM logs_admin l LEFT JOIN usuarios u ON l.adminId = u.id " +
                "ORDER BY l.id DESC", null
        )
        while (c.moveToNext()) {
            lista.add(
                LogAdmin(
                    c.getInt(0), c.getInt(1), c.getString(2),
                    c.getString(3), c.getString(4),
                    c.getString(5) ?: "Admin"
                )
            )
        }
        c.close()
        return lista
    }

    fun contarLogs(): Int = contar("logs_admin")

    // ---------------- ADMIN: PAPELERA ----------------

    fun moverAPapelera(tipo: String, contenido: String, eliminadoPor: Int) {
        val v = ContentValues().apply {
            put("tipo", tipo)
            put("contenido", contenido)
            put("fechaEliminacion", System.currentTimeMillis().toString())
            put("eliminadoPor", eliminadoPor)
        }
        writableDatabase.insert("papelera", null, v)
    }

    // ---------------- ADMIN: STATS ----------------

    fun obtenerNombreUsuario(id: Int): String {
        val c = readableDatabase.rawQuery(
            "SELECT nombre FROM usuarios WHERE id = ?", arrayOf(id.toString())
        )
        var nombre = "Sistema"
        if (c.moveToFirst()) nombre = c.getString(0) ?: "Sistema"
        c.close()
        return nombre
    }

    // ---------------- UTILIDADES ----------------

    private fun contar(tabla: String): Int {
        val c = readableDatabase.rawQuery("SELECT COUNT(*) FROM $tabla", null)
        c.moveToFirst()
        val total = c.getInt(0)
        c.close()
        return total
    }
}

data class ItemCompra(
    val id: Int,
    val item: String,
    val precio: Double,
    val comprado: Boolean
)
