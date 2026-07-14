# RECETAS LID — App móvil (Proyecto Final, Desarrollo de Software VI)

App Android que sugiere recetas a partir de los ingredientes que el usuario ya tiene,
prioriza opciones económicas y arma una lista de compras solo con lo que falta.

**Versión:** v0.9 (beta — Tercera Revisión, ~70% de funcionalidades clave)
**Grupo:** 1GS231 · **Profesor:** Giovani Sánchez

## Tecnologías
- Lenguaje: Kotlin
- IDE: Android Studio
- ViewBinding para el enlace de vistas
- Persistencia local: SQLite (SQLiteOpenHelper) + SharedPreferences (sesión)
- Material Design + modo oscuro (values-night)
- minSdk 24 · targetSdk 34

## Cómo abrir y ejecutar
1. Abrir Android Studio → **File > Open** y seleccionar la carpeta `RecetasLID`.
2. Esperar el **Gradle Sync** (descarga dependencias automáticamente).
3. Conectar un dispositivo o iniciar un emulador (API 24 o superior).
4. Presionar **Run ▶**.
5. Para generar el APK: **Build > Build Bundle(s) / APK(s) > Build APK(s)**.

## Cuentas de prueba (precargadas)
| Rol | Correo | Contraseña |
|-----|--------|-----------|
| Administrador | admin | Admin123 |
| Usuario | leo@correo.com | 123456 |

También puedes crear una cuenta nueva desde la pantalla de registro.

## Estructura del proyecto
```
app/src/main/java/com/utp/recetaslid/
  data/      -> DBHelper (SQLite), SessionManager, SeedData
  model/     -> Usuario, Receta
  adapter/   -> RecetaAdapter, UsuarioAdapter, CompraAdapter
  ui/        -> Activities (Splash, Login, Registro, Main, Resultados,
                DetalleReceta, CrearReceta, Favoritos, Compras, Admin)
res/         -> layouts, colores, temas (claro/oscuro), drawables, iconos
```

## Funcionalidades implementadas
- Registro e inicio de sesión con roles (usuario / administrador)
- Búsqueda de recetas por ingredientes ("¿qué hay en mi nevera?")
- Filtro de recetas económicas
- Detalle de receta (marca lo que tienes vs. lo que te falta)
- Crear receta propia con validaciones
- Favoritos por usuario
- Lista de compras con total y marcar comprado
- Panel de administrador (estadísticas, gestión de usuarios, moderación)
- Modo oscuro automático
