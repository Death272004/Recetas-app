# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

This is an Android project — all building, running, and testing happens through **Android Studio**, not the terminal.

- **Build:** `Build → Make Project` (or Ctrl+F9)
- **Run on emulator/device:** `Run → Run 'app'` (Shift+F10)
- **Clean build:** `Build → Clean Project`, then `Build → Rebuild Project`
- **Sync after Gradle changes:** `File → Sync Project with Gradle Files`

Toolchain: AGP 8.1.4, Gradle 8.2, Kotlin 1.9.10, JDK 17, compileSdk/targetSdk 34, minSdk 24.

When the DB schema changes, bump `DBHelper` version (currently **5**) and **reinstall the app** — `onUpgrade` drops and recreates all tables, which also re-runs `SeedData.cargar()`.

To inspect the live SQLite DB: **Android Studio → App Inspection → Database Inspector**.

## Architecture

**Language/stack:** Kotlin, Android SDK 34 (min 24), ViewBinding, raw SQLite (no Room/Flow).

### App flow

```
SplashActivity (LAUNCHER)
    ├── no session  → LoginActivity → RegistroActivity / RecuperarClaveActivity
    │                 LoginActivity → "Continuar sin cuenta" → MainActivity (guest)
    ├── admin role  → AdminActivity
    └── user role   → MainActivity
```

`SessionManager` (SharedPreferences key `sesion_recetas_lid`) holds `usuario_id`, `nombre`, `rol`.

### Guest mode

`MainActivity`, `ResultadosActivity` and `FeedActivity` are browsable without a session.
Gated actions (open recipe detail, like, favorites) call **`LoginRapido.mostrar(activity, mensaje) { ... }`** —
an `AlertDialog` built from `dialog_login.xml` that logs in inline and then runs the pending action
via the `alEntrar` callback. Admins logging in through the dialog are redirected to `AdminActivity`.
Prefer this over a Toast when a guest hits a gated action.

### Package layout

| Package | Contents |
|---|---|
| `data/` | `DBHelper` (SQLite), `SeedData`, `SessionManager`, `Seguridad` (password hashing) |
| `model/` | `Receta`, `Usuario`, `Comentario`, `FeedPost`, `LogAdmin` |
| `adapter/` | `RecetaAdapter`, `CompraAdapter`, `UsuarioAdapter`, `FeedAdapter`, `ComentarioAdapter`, `Admin*Adapter` |
| `ui/` | All Activities + `LoginRapido` (object, not an Activity — do not declare in the Manifest) |

### Database (`recetas_lid.db`, version 5)

Seven tables: `usuarios`, `recetas`, `favoritos`, `compras`, `comentarios`, `logs_admin`, `papelera`.

- `usuarios`: id, nombre, correo (UNIQUE), clave (**SHA-256 hash**), rol (`"admin"` | `"usuario"`), estado, fechaRegistro, ultimoAcceso, pregunta, respuesta (**hashed**, lowercased+trimmed)
- `recetas`: id, titulo, ingredientes (comma-separated string), pasos, tiempo (min), costo (B/.), autorId, reportada, imagen (drawable name, no extension), videoUrl, oculta, destacada
- `favoritos`: usuarioId, recetaId (many-to-many join)
- `compras`: usuarioId, item, precio, comprado (0/1)
- `comentarios`: usuarioId, recetaId, texto, fecha, estado
- `logs_admin`: adminId, accion, detalle, fecha
- `papelera`: tipo, contenido, fechaEliminacion, eliminadoPor

All queries use `rawQuery` with `?` placeholders. `cursorAReceta()` maps columns by name.

### Passwords (RNF-03)

**Never store or compare plaintext passwords.** `Seguridad.hashClave(clave)` returns SHA-256 of `SAL + clave`.
`registrarUsuario`, `login`, `resetearClave`, `obtenerEstadoPorCredenciales`, `cambiarClave`,
`verificarRespuesta` and `SeedData.insertarUsuario` all hash before touching the DB —
callers pass the plaintext and let `DBHelper` do the hashing.

### Password recovery

`RecuperarClaveActivity` — 3 panels in one layout (`panelPaso1/2/3`, toggled with `visibility`):
correo → security question → new password. Max 3 wrong answers, then the screen closes.
Questions are defined in `RegistroActivity.preguntas`.

### Navigation

`MainActivity` has a bottom nav bar (`navHome`, `navFeed`, `navFavoritos`, `navOtros`).
Navigation is manual `startActivity()` — no Jetpack Navigation component. Every secondary screen has `btnVolver` → `finish()`.

`CrearRecetaActivity` doubles as the edit screen (RF-07): pass an `recetaId` extra to enter edit mode
(it loads the recipe, retitles the screen, and calls `db.actualizarReceta()` instead of `insertarReceta()`).
It rejects editing recipes the user doesn't own.

### Images & video

- Recipe images: drawable resource names in the `imagen` column, resolved with `getIdentifier(name, "drawable", packageName)`. Falls back to `ic_receta` on `naranja_suave`.
- Videos: `videoUrl` holds a full YouTube watch URL, opened via `Intent(ACTION_VIEW, ...)`.
- Food photo drawables: `arroz_frito.jpg`, `ensalada_simple.jpg`, `pollo_guisado.jpg`.

### Colors (brand palette)

`naranja` (#F2541B) is the primary brand color. Secondary: `naranja_oscuro`, `naranja_suave`.
Neutral: `negro`, `gris`, `gris_claro`, `linea`, `blanco`. Status: `verde`.

### Seeded accounts

`admin` / `Admin123` (rol admin) and `leo@correo.com` / `123456` (rol usuario).
Security answer for both: `firulais`.

## Key constraints

- No Room, no Coroutines, no LiveData — DB calls stay synchronous on the main thread (acceptable for this academic project).
- `Receta.ingredientes` is a plain comma-separated string; `listaIngredientes()` splits it.
- Comments in the code are written in Spanish, beginner-friendly, per the professor's conventions.
