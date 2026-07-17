package com.utp.recetaslid

import android.app.Application
import com.utp.recetaslid.data.ThemeManager

class RecetasLidApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeManager.aplicarModoGuardado(this)
    }
}
