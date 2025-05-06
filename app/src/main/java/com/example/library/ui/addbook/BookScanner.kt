package com.example.library.ui.addbook

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.example.library.R
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

// A BookScanner osztály a könyvek szkennelésére szolgál az ISBN kódok olvasásához
class BookScanner(private val activity: Activity) {

    // A startScan metódus indítja el a szkennelést
    fun startScan() {
        // Inicializáljuk az IntentIntegrator-t, amely a szkennelési műveletet végzi
        val integrator = IntentIntegrator(activity)

        // Beállítjuk a prompt szöveget, amely a szkennelés előtt jelenik meg
        integrator.setPrompt(activity.getString(R.string.scan_isbn_prompt))

        // Bekapcsoljuk a hangjelzést, hogy a szkennelés eredményét jelezzük
        integrator.setBeepEnabled(true)

        // Lehetővé tesszük, hogy a tájolás ne legyen lezárva a szkennelés során
        integrator.setOrientationLocked(false)

        // Csak vonalkód típusokat szeretnénk olvasni (nem QR kódot, hanem az ISBN típusú vonalkódot)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES)

        // Elindítjuk a szkennelést
        integrator.initiateScan()
    }

    // A handleResult metódus kezeli a szkennelés eredményét
    fun handleResult(
        requestCode: Int,  // A kérés kódja
        resultCode: Int,   // Az eredmény kódja (sikeres vagy nem)
        data: Intent?,     // Az Intent, amely tartalmazza az eredményeket
        onScanned: (isbn: String) -> Unit  // Egy callback függvény, amely az ISBN-t adja vissza
    ): Boolean {
        // Parse-oljuk a szkennelés eredményét az IntentIntegrator segítségével
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        // Ha van eredmény
        return if (result != null) {
            // Ha a szkennelés eredménye üres (nem történt semmi), akkor értesítjük a felhasználót
            if (result.contents == null) {
                Toast.makeText(activity, activity.getString(R.string.scan_cancelled), Toast.LENGTH_SHORT).show()
            } else {
                // Ha van érvényes ISBN, meghívjuk a callback függvényt az ISBN kóddal
                onScanned(result.contents)
            }
            // A metódus kezelte az eredményt
            true
        } else {
            // Ha az eredmény nem lett feldolgozva, akkor nem történt semmi
            false
        }
    }
}
