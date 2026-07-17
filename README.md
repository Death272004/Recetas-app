# RECETAS LID — App móvil (Proyecto Final, Desarrollo de Software VI)

App Android que sugiere recetas a partir de los ingredientes que el usuario ya tiene,
prioriza opciones económicas y arma una lista de compras solo con lo que falta.

**Versión:** v1.0 (entrega final — 100% de los requerimientos funcionales)
**Grupo:** 1GS231 · **Profesor:** Giovani Sánchez

## Tecnologías
- Lenguaje: Kotlin
- IDE: Android Studio
- ViewBinding para el enlace de vistas
- Persistencia local: SQLite (SQLiteOpenHelper) + SharedPreferences (sesión)
- Seguridad: contraseñas almacenadas con hash SHA-256 + sal
- Material Design + modo oscuro (values-night)
- minSdk 24 · targetSdk 34 · AGP 8.1.4 · Gradle 8.2 · Kotlin 1.9.10

## Cómo abrir y ejecutar
1. Abrir Android Studio → **File > Open** y seleccionar la carpeta `RecetasLID`.
2. Esperar el **Gradle Sync** (descarga dependencias automáticamente).
3. Conectar un dispositivo o iniciar un emulador (API 24 o superior).
4. Presionar **Run ▶**.
5. Para generar el APK: **Build > Build Bundle(s) / APK(s) > Build APK(s)**.

> Si ya tenías la app instalada de una versión anterior, **desinstálala primero**.
> La base de datos subió a la versión 5 (las contraseñas ahora se guardan cifradas) y
> `onUpgrade` recrea las tablas y vuelve a cargar los datos de ejemplo.

## Cuentas de prueba (precargadas)
| Rol | Correo | Contraseña |
|-----|--------|-----------|
| Administrador | admin | Admin123 |
| Usuario | leo@correo.com | 123456 |

Respuesta de seguridad de ambas cuentas: **firulais**
(pregunta: "¿Nombre de tu primera mascota?")

También puedes crear una cuenta nueva desde la pantalla de registro.

## Modo invitado
La pantalla principal muestra el catálogo de recetas **sin necesidad de iniciar sesión**.
Al tocar una receta, dar like o entrar a favoritos, aparece una **mini pantalla de inicio
de sesión** (diálogo) sin salir de donde estabas; al entrar, la app continúa
automáticamente con la acción que ibas a hacer.

## Estructura del proyecto
```
app/src/main/java/com/utp/recetaslid/
  data/      -> DBHelper (SQLite), SessionManager, SeedData, Seguridad (hash)
  model/     -> Usuario, Receta, Comentario, FeedPost, LogAdmin
  adapter/   -> RecetaAdapter, UsuarioAdapter, CompraAdapter, FeedAdapter,
                ComentarioAdapter, AdminUsuarioAdapter, AdminRecetaAdapter,
                AdminComentarioAdapter, AdminLogAdapter
  ui/        -> 27 Activities + LoginRapido (mini pantalla de login)
res/         -> layouts, colores, temas (claro/oscuro), drawables, iconos
```

## Requerimientos funcionales cubiertos
| RF | Descripción | Estado |
|----|-------------|--------|
| RF-01 | Registrar cuenta (nombre, correo, contraseña) | ✅ |
| RF-02 | Iniciar/cerrar sesión con roles usuario y administrador | ✅ |
| RF-03 | Ingresar los ingredientes disponibles | ✅ |
| RF-04 | Sugerir recetas según los ingredientes | ✅ |
| RF-05 | Filtrar recetas económicas / de pocos ingredientes | ✅ |
| RF-06 | Ver detalle de receta (ingredientes, pasos, tiempo, costo) | ✅ |
| RF-07 | Crear, **editar** y eliminar recetas propias | ✅ |
| RF-08 | Marcar y consultar favoritas | ✅ |
| RF-09 | Lista de compras con los ingredientes faltantes | ✅ |
| RF-10 | Publicar y consultar recetas en el feed de comunidad | ✅ |
| RF-11 | Administrador gestiona usuarios y modera recetas | ✅ |

## Funcionalidades adicionales
- Recuperación de contraseña por pregunta de seguridad (3 pasos, máximo 3 intentos)
- Cambio de contraseña desde el perfil (validando la contraseña actual)
- Comentarios en recetas y moderación por el administrador
- Perfiles públicos, logros, mi actividad, notificaciones y privacidad
- Panel de administración con estadísticas, logs de acciones y papelera
- Video de la receta (se abre en YouTube)
- Modo oscuro automático
