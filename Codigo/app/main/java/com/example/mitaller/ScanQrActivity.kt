package com.example.mitaller

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class ScanQrActivity : AppCompatActivity() {

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            // Resultado escaneado, lo devolvemos a HomeActivity
            val intent = Intent()
            intent.putExtra("QR_RESULT", result.contents)
            setResult(Activity.RESULT_OK, intent)
        } else {
            // Escaneo cancelado
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        iniciarEscaneo()
    }

    private fun iniciarEscaneo() {
        val options = ScanOptions()
        options.setPrompt("Escaneá el código QR")
        options.setBeepEnabled(true)
        options.setOrientationLocked(true) // ✅ Bloqueamos en modo vertical
        options.setCaptureActivity(PortraitCaptureActivity::class.java) // ✅ Usamos clase vertical personalizada
        barcodeLauncher.launch(options)
    }
}
