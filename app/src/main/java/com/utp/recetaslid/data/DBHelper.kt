package com.utp.recetaslid.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.utp.recetaslid.model.Receta
import com.utp.recetaslid.model.Usuario

// Base de datos local de la aplicacion (SQLite)
class DBHelper(context: Context) :
    SQLiteOpenHelper(context, "recetas_lid.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        // Tabla de usuarios
        db.execSQL(
            "CREATE TABLE usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT, correo TEXT UNIQUE, clave TEXT, rol TEXT)"
        )
        // Tabla de recetas
        db.execSQL(
            "CREATE TABLE recetas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "titulo TEXT, ingredientes TEXT, pasos TEXT, " +
                "tiempo INTEGER, costo REAL, autorId INTEGER, reportada INTEGER DEFAULT 0, " +
                "imagen TEXT, videoUrl TEXT)"
        )
        // Tabla de favoritos (relacion usuario - receta)
        db.execSQL(
            "CREATE TABLE favoritos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, usuarioId INTEGER, recetaId INTEGER)"
        )
        // Tabla de lista de compras
        db.execSQL(
            "CREATE TABLE compras (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, usuarioId INTEGER, " +
                "item TEXT, precio REAL, comprado INTEGER DEFAULT 0)"
        )
        // Cargamos los datos iniciales (admin + recetas de ejemplo)
        SeedData.cargar(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS usuarios")
        db.execSQL("DROP TABLE IF EXISTS recetas")
        db.execSQL("DROP TABLE IF EXISTS favoritos")
        db.execSQL("DROP TABLE IF EXISTS compras")
        onCreate(db)
    }

    // ---------------- USUARIOS ----------------

    // Registra un usuario nuevo. Devuelve true si se creo correctamente
    fun registrarUsuario(nombre: String, correo: String, clave: String): Boolean {
        if (existeCorreo(correo)) return false
        val valores = ContentValues().apply {
            put("nombre", nombre)
            put("correo", correo)
            put("clave", clave)
            put("rol", "usuario")
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

    // Valida las credenciales y devuelve el usuario si son correctas
    fun login(correo: String, clave: String): Usuario? {
        val c = readableDatabase.rawQuery(
            "SELECT id, nombre, correo, clave, rol FROM usuarios WHERE correo = ? AND clave = ?",
            arrayOf(correo, clave)
        )
        var usuario: Usuario? = null
        if (c.moveToFirst()) {
            usuario = Usuario(
                c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4)
            )
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
        val c = readableDatabase.rawQuery("SELECT * FROM recetas ORDER BY id DESC", null)
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

    // Busca recetas que usen al menos uno de los ingredientes indicados.
    // Si soloEconomicas es true, filtra por costo bajo o pocos ingredientes.
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
        // Ordenamos por menor cantidad de ingredientes faltantes y luego por costo
        return resultado.sortedWith(
            compareBy({ faltantes(it, buscados).size }, { it.costo })
        )
    }

    // Devuelve los ingredientes de la receta que NO estan en la lista del usuario
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
            c.getString(c.getColumnIndexOrThrow("videoUrl")) ?: ""
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

    // Agrega o quita de favoritos. Devuelve el nuevo estado (true = es favorito)
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
        // Evitamos duplicados del mismo item para el usuario
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

    // Devuelve la lista de compras del usuario como tripletas (id, item, precio, comprado)
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

    // ---------------- PERFIL / ESTADISTICAS ----------------

    fun obtenerUsuario(id: Int): Usuario? {
        val c = readableDatabase.rawQuery(
            "SELECT id, nombre, correo, clave, rol FROM usuarios WHERE id = ?", arrayOf(id.toString())
        )
        var u: Usuario? = null
        if (c.moveToFirst()) {
            u = Usuario(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4))
        }
        c.close()
        return u
    }

    fun actualizarNombre(id: Int, nombre: String) {
        val v = ContentValues().apply { put("nombre", nombre) }
        writableDatabase.update("usuarios", v, "id = ?", arrayOf(id.toString()))
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
            "SELECT * FROM recetas WHERE titulo LIKE ? ORDER BY id DESC", arrayOf("%$query%")
        )
        while (c.moveToNext()) lista.add(cursorAReceta(c))
        c.close()
        return lista
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

// Clase simple para representar un item de la lista de compras
data class ItemCompra(
    val id: Int,
    val item: String,
    val precio: Double,
    val comprado: Boolean
)
