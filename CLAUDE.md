# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

This is an Android project — all building, running, and testing happens through **Android Studio**, not the terminal.

- **Build:** `Build → Make Project` (or Ctrl+F9)
- **Run on emulator/device:** `Run → Run 'app'` (Shift+F10)
- **Clean build:** `Build → Clean Project`, then `Build → Rebuild Project`
- **Sync after Gradle changes:** `File → Sync Project with Gradle Files`

When the DB schema changes (tables added/columns added), bump `DBHelper` version number (currently `2`) and **reinstall the app** on the device — `onUpgrade` drops and recreates all tables, which also re-runs `SeedData.cargar()`.

To inspect the live SQLite DB: **Android Studio → App Inspection → Database Inspector** (device must be connected and app running).

## Architecture

**Language/stack:** Kotlin, Android SDK 34 (min 24), ViewBinding, raw SQLite (no Room/Flow).

### App flow

```
SplashActivity (LAUNCHER)
    ├── no session  → LoginActivity → RegistroActivity
    ├── admin role  → AdminActivity
    └── user role   → MainActivity
```

`SessionManager` (SharedPreferences key `sesion_recetas_lid`) holds `usuario_id`, `nombre`, `rol` for the active session.

### Package layout

| Package | Contents |
|---|---|
| `data/` | `DBHelper` (SQLite), `SeedData` (seed on first install), `SessionManager` |
| `model/` | `Receta`, `Usuario`, `FeedPost` |
| `adapter/` | `RecetaAdapter`, `CompraAdapter`, `UsuarioAdapter`, `FeedAdapter` |
| `ui/` | All Activities |

### Database (`recetas_lid.db`, version 2)

Four tables: `usuarios`, `recetas`, `favoritos`, `compras`.

- `usuarios`: id, nombre, correo (UNIQUE), clave (plaintext), rol (`"admin"` | `"usuario"`)
- `recetas`: id, titulo, ingredientes (comma-separated string), pasos, tiempo (min), costo (B/.), autorId, reportada, imagen (drawable name, no extension), videoUrl (full YouTube watch URL)
- `favoritos`: usuarioId, recetaId (many-to-many join)
- `compras`: usuarioId, item, precio, comprado (0/1)

All queries use `rawQuery` with `?` placeholders. `cursorAReceta()` in `DBHelper` maps columns by name using `getColumnIndexOrThrow`.

### Navigation

`MainActivity` has a bottom nav bar (4 plain `TextView` buttons: `navHome`, `navFeed`, `navFavoritos`, `navOtros`). Navigation is manual `startActivity()` — no Jetpack Navigation component. Every secondary screen has a `btnVolver` that calls `finish()`.

### Images & video

- Recipe images: stored as drawable resource names in the `imagen` column. Loaded at runtime with `getIdentifier(name, "drawable", packageName)`. Falls back to `ic_receta` icon with orange background if name is empty or not found.
- Videos: `videoUrl` holds a full YouTube watch URL. Opens via `Intent(ACTION_VIEW, Uri.parse(url))` — no WebView embedding.
- Food photo drawables: `arroz_frito.jpg`, `ensalada_simple.jpg`, `pollo_guisado.jpg` in `res/drawable/`.

### Colors (brand palette)

`naranja` (#F2541B) is the primary brand color used for headers, buttons, and accents. Secondary: `naranja_oscuro`, `naranja_suave`. Neutral: `negro`, `gris`, `gris_claro`, `linea`, `blanco`. Status: `verde`.

### Admin user

Seeded by `SeedData.cargar()` with correo `admin@lid.com`, clave `admin123`, rol `admin`. Admin sees `AdminActivity` (user list + reported recipes).

## Key constraints

- `RecetasLID/` (sibling folder) is the **original backup** — never modify it.
- No Room, no Coroutines, no LiveData — keep all DB calls synchronous on the main thread (acceptable for this academic project).
- `Receta.ingredientes` is a plain comma-separated string; `listaIngredientes()` splits it. There is no separate ingredients table yet.
- The `economicas` intent extra is still passed from `MainActivity` (hardcoded `false`) and read in `ResultadosActivity` / `DBHelper.buscarPorIngredientes()` but not displayed in the UI.
