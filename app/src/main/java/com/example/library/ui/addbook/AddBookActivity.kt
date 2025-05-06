package com.example.library.ui.addbook

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.library.googleapi.ApiClient
import com.example.library.db.Book
import com.example.library.viewmodel.BookViewModel
import com.example.library.googleapi.GoogleBooksResponse
import com.example.library.R
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class AddBookActivity : AppCompatActivity() {

    // UI elemek deklarálása
    lateinit var title: EditText
    lateinit var author: EditText
    lateinit var date: EditText
    lateinit var image: ImageView
    lateinit var category: Spinner
    lateinit var language: Spinner
    lateinit var type: RadioGroup
    lateinit var location: EditText
    lateinit var condition: RadioGroup
    lateinit var status: Spinner
    lateinit var isbn: EditText

    lateinit var cancel: Button
    lateinit var save: Button

    // Egyéb változók
    private var id = -1
    private var isUpdate = false
    private val CAMERA_REQUEST_CODE = 100
    private val GALLERY_REQUEST_CODE = 101
    private var selectedImageUri: Uri? = null
    private var scannedIsbn: String? = null

    private lateinit var bookScanner: BookScanner

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Layout hozzárendelése az Activity-hez
        setContentView(R.layout.activity_add_book)

        // Könyvszkenner inicializálása (pl. ISBN olvasásra)
        bookScanner = BookScanner(this)

        // Csak teszteléshez:
/*                val scannedIsbn = "9781582701707"
                fetchBookDataAndSave(scannedIsbn)*/

        //Log.d("AddBookActivity", "onCreate() - Activity started")

        // UI elemek inicializálása
        title = findViewById(R.id.book_title)
        author = findViewById(R.id.book_author)
        date = findViewById(R.id.book_date)
        image = findViewById(R.id.book_image)
        category = findViewById(R.id.book_category)
        language = findViewById(R.id.book_language)
        type = findViewById(R.id.book_type)
        location = findViewById(R.id.book_location)
        condition = findViewById(R.id.book_condition)
        status = findViewById(R.id.book_status)
        isbn = findViewById(R.id.book_isbn)

        cancel = findViewById(R.id.buttonCancel)
        save = findViewById(R.id.buttonSave)

        // Toolbar beállítása
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Ellenőrizzük, hogy "szerkesztés módban" vagyunk-e
        isUpdate = intent.getBooleanExtra("isUpdate", false)
        //Log.d("AddBookActivity", "onCreate() - isUpdate: $isUpdate")

        // Spinner adapterek beállítása kategóriákhoz, nyelvekhez, státuszhoz
        val categoryAdapter = ArrayAdapter.createFromResource(
            this, R.array.categories, android.R.layout.simple_spinner_item
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        category.adapter = categoryAdapter

        val languageAdapter = ArrayAdapter.createFromResource(
            this, R.array.languages, android.R.layout.simple_spinner_item
        )
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        language.adapter = languageAdapter

        val statusAdapter = ArrayAdapter.createFromResource(
            this, R.array.statuses, android.R.layout.simple_spinner_item
        )
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        status.adapter = statusAdapter

        // Kép készítése kamerával
        findViewById<Button>(R.id.buttonCaptureImage).setOnClickListener {
            if (checkPermissions()) {
                openCamera()
            }
        }

        // Kép kiválasztása galériából
        findViewById<Button>(R.id.buttonSelectImage).setOnClickListener {
            if (checkPermissions()) {
                openGallery()
            }
        }

        // Ha update módban vagyunk, akkor az adatokat betöltjük az űrlapra
        val isUpdate = intent.hasExtra("id") // Ellenőrizzük, hogy frissítés módban vagyunk-e

        if (isUpdate) {
            // Frissítés esetén, előtöltjük a mezőket
            id = intent.getIntExtra("id", -1)
            supportActionBar?.title = getString(R.string.update_book)
            title.setText(intent.getStringExtra("title"))
            author.setText(intent.getStringExtra("author"))
            date.setText(intent.getStringExtra("date"))
            location.setText(intent.getStringExtra("location"))
            isbn.setText(intent.getStringExtra("isbn"))

            // Kép beállítása URI alapján
            intent.getStringExtra("image")?.let {
                try {
                    val uri = Uri.parse(it)
                    image.setImageURI(uri)
                    selectedImageUri = uri
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Típus és állapot beállítása
            setRadioButtonValue(type, intent.getStringExtra("type"))
            setRadioButtonValue(condition, intent.getStringExtra("condition"))

            // Spinner értékek kiválasztása
            setSpinnerSelection(category, intent.getStringExtra("category"))
            setSpinnerSelection(language, intent.getStringExtra("language"))
            setSpinnerSelection(status, intent.getStringExtra("status"))

            // Frissítés gomb működése
            save.setOnClickListener {
                val enteredTitle = title.text.toString()
                val enteredAuthor = author.text.toString()
                val bookViewModel: BookViewModel by viewModels()

                // Ellenőrzés: létezik-e már a könyv az adatbázisban, figyelembe véve az ID-t
                bookViewModel.getBookByTitleAndAuthorExcludingId(enteredTitle, enteredAuthor, id)
                    .observe(this) { existingBook ->
                        if (existingBook != null) {
                            // Ha már létezik, akkor mutassunk üzenetet
                            Toast.makeText(this, getString(R.string.book_already_exists), Toast.LENGTH_SHORT).show()
                        } else {
                            // Ha nem létezik, akkor folytathatjuk a frissítést
                            saveOrUpdateBook(isUpdate = true) // Frissítés
                        }
                    }
            }

        } else {
            // Új könyv hozzáadása
            supportActionBar?.title = getString(R.string.add_new_book)

            save.setOnClickListener {
                val enteredTitle = title.text.toString()
                val enteredAuthor = author.text.toString()
                val bookViewModel = ViewModelProvider(this).get(BookViewModel::class.java)

                // Ellenőrzés: létezik-e már a könyv az adatbázisban
                bookViewModel.getBookByTitleAndAuthor(enteredTitle, enteredAuthor)
                    .observe(this) { existingBook ->
                        if (existingBook != null) {
                            // Ha már létezik, akkor mutassunk üzenetet
                            Toast.makeText(this, getString(R.string.book_already_exists), Toast.LENGTH_SHORT).show()
                        } else {
                            // Ha nem létezik, akkor menthetjük a könyvet
                            saveOrUpdateBook(isUpdate = false) // Új könyv
                        }
                    }
            }
        }

        // Mégse gomb
        cancel.setOnClickListener {
            Toast.makeText(applicationContext, getString(R.string.nothing_saved), Toast.LENGTH_SHORT).show()
            finish()
        }

        // Hátteret beállítjuk a shared preferences alapján
        val backgroundLayout = findViewById<FrameLayout>(R.id.addBookBackground)
        val prefs = getSharedPreferences("wallpaper_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("wallpaper_uri", null)

        uriString?.let { uriStr ->
            try {
                val uri = Uri.parse(uriStr)
                val inputStream = contentResolver.openInputStream(uri)
                val drawable = Drawable.createFromStream(inputStream, uri.toString())
                drawable?.let { bg ->
                    backgroundLayout.background = bg
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Feldolgozzuk az ISBN szkenner eredményét (ZXing használatával)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                // Ha sikeresen beolvasott egy ISBN-t
                scannedIsbn = result.contents
                Toast.makeText(
                    this,
                    getString(R.string.scan_successful, scannedIsbn),
                    Toast.LENGTH_SHORT
                ).show()

                // Könyvadatok lekérése az ISBN alapján
                fetchBookDataAndSave(scannedIsbn!!)
            } else {
                // Ha a felhasználó megszakította a szkennelést
                Toast.makeText(this, getString(R.string.scan_cancelled), Toast.LENGTH_SHORT).show()
            }
        } else {
            // Más eredmény feldolgozása
            super.onActivityResult(requestCode, resultCode, data)
        }

        // Kamera vagy galéria eredményének kezelése
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    // Ha a kép sikeresen el lett készítve
                    val file = File(selectedImageUri!!.path!!)
                    if (file.exists()) {
                        image.setImageURI(selectedImageUri)
                    } else {
                        Log.e(
                            "AddBookActivity",
                            "Kép fájl nem található: ${selectedImageUri!!.path}"
                        )
                    }
                }

                GALLERY_REQUEST_CODE -> {
                    // Ha a felhasználó képet választott a galériából
                    val imageUri = data?.data
                    if (imageUri != null) {
                        // Átmásoljuk a képet a belső tárolóba
                        val copiedUri = copyUriToInternalStorage(imageUri)
                        if (copiedUri != null) {
                            image.setImageURI(copiedUri)
                            selectedImageUri = copiedUri
                        } else {
                            Toast.makeText(
                                this,
                                getString(R.string.image_copy_error),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Menü beállítása a Toolbar-hoz
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // Ha az ISBN szkennelés gombra kattint a felhasználó
            R.id.action_scan_isbn -> {
                bookScanner.startScan() // Elindítjuk a szkennelést
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Engedélykérés eredményének kezelése
        if (requestCode == 0) {
            if (grantResults.isNotEmpty()) {
                val granted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                if (granted) {
                    // Ha minden szükséges engedély meg van adva
                    Toast.makeText(
                        this,
                        getString(R.string.permissions_granted),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Ha valamelyik engedélyt megtagadták
                    Toast.makeText(this, getString(R.string.permissions_denied), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    // Könyv mentése vagy frissítése a megadott adatok alapján
    private fun saveOrUpdateBook(isUpdate: Boolean) {
        // Felhasználótól bekért adatok lekérése
        val title = title.text.toString()
        val author = author.text.toString()
        val date = date.text.toString()
        // Kép elérési útvonala: ha új kép van kiválasztva, akkor azt használja, különben az eredeti képet
        val image = selectedImageUri?.toString() ?: intent.getStringExtra("image")
        val category = category.selectedItem.toString()
        val language = language.selectedItem.toString()
        val isbn = isbn.text.toString()

        val type = getSelectedRadioButtonText(R.id.book_type)
        val location = location.text.toString()
        val condition = getSelectedRadioButtonText(R.id.book_condition)
        val status = status.selectedItem.toString()

        // Kötelező mezők ellenőrzése: cím és szerző nem lehet üres
        if (title.isEmpty() || author.isEmpty()) {
            Toast.makeText(applicationContext, getString(R.string.title_and_author_empty), Toast.LENGTH_SHORT).show()
            return
        }

        // Dátum formátum ellenőrzése, ha meg van adva
        if (date.isNotEmpty() && !isValidDate(date)) {
            Toast.makeText(applicationContext, getString(R.string.invalid_date_format), Toast.LENGTH_SHORT).show()
            return
        }

        // Új Intent létrehozása, amivel visszaküldjük az adatokat a hívó Activity-nek
        val intent = Intent().apply {
            putExtra("title", title)
            putExtra("author", author)
            putExtra("date", date)
            putExtra("image", image)
            putExtra("category", category)
            putExtra("language", language)
            putExtra("type", type)
            putExtra("location", location)
            putExtra("condition", condition)
            putExtra("status", status)
            putExtra("isbn", isbn)
        }

        // Ha frissítésről van szó, adjuk hozzá az eredeti azonosítót is
        if (isUpdate) {
            intent.putExtra("id", id) // Csak frissítéskor
        }

        // Beállítjuk az eredményt, és bezárjuk az Activity-t
        setResult(RESULT_OK, intent)
        finish()
    }

    // Lekéri a kiválasztott RadioButton szövegét egy megadott RadioGroup-ból
    private fun getSelectedRadioButtonText(groupId: Int): String {
        // Lekéri a RadioGroup-ot a megadott azonosító alapján
        val radioGroup = findViewById<RadioGroup>(groupId)
        // Lekéri a kiválasztott RadioButton azonosítóját, ha van ilyen
        val selectedRadioButtonId = radioGroup.checkedRadioButtonId
        // Ha van kiválasztott RadioButton
        return if (selectedRadioButtonId != -1) {
            // Lekéri a kiválasztott RadioButton-t
            val selectedRadioButton = findViewById<RadioButton>(selectedRadioButtonId)
            // Visszaadja a RadioButton szövegét String-ként
            selectedRadioButton.text.toString()
        } else {
            // Ha nincs semmi kiválasztva, üres stringgel tér vissza
            ""
        }
    }

    private fun isValidDate(date: String): Boolean {
        // Dátum validáció: pontosan 4 számjegy (év)
        val dateRegex = Regex("^\\d{4}\$")
        return dateRegex.matches(date)
    }

    // Megnyitja a kameraalkalmazást képrögzítéshez
    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            // Létrehozzuk a képfájlt
            val photoFile = createImageFile()
            // Elérési út URI-t generálunk a fájlhoz a FileProvider segítségével
            val photoURI: Uri = FileProvider.getUriForFile(
                this, "com.example.library.fileprovider.v2", photoFile
            )
            selectedImageUri = photoURI // Ez szükséges, hogy később tudjuk, mit töltöttünk fel
            // A kameraalkalmazásnak megmondjuk, hova mentse a képet
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        } else {
            Toast.makeText(this, getString(R.string.no_camera_app_available), Toast.LENGTH_SHORT)
                .show()
        }
    }

    // Létrehoz egy ideiglenes fájlt a kép mentéséhez az eszköz külső tárhelyére
    private fun createImageFile(): File {
        val timestamp = System.currentTimeMillis()
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timestamp}_", ".jpg", storageDir
        )
    }

    // Megnyitja a galériát egy kép kiválasztásához
    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
    }

    // Ellenőrzi, hogy a kamera engedély meg van-e adva, ha nincs, kér engedélyt
    private fun checkPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

        // Az olvasási és írási engedélyek eltávolítása Android 11-től kezdődően
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA), 0
            )
            return false
        }
        return true
    }

    // Lekéri a könyv adatait Google Books API-ból az ISBN alapján, és elmenti, ha még nem létezik
    private fun fetchBookDataAndSave(isbn: String) {

        Toast.makeText(
            applicationContext,
            getString(R.string.fetch_starting_isbn, isbn),
            Toast.LENGTH_SHORT
        ).show()

        val api = ApiClient.googleBooksApi
        val call = api.getBookByIsbn("isbn:$isbn")

        call.enqueue(object : Callback<GoogleBooksResponse> {
            override fun onResponse(
                call: Call<GoogleBooksResponse>,
                response: Response<GoogleBooksResponse>
            ) {

                //Log.d("AddBookActivity", "API válasz érkezett.")
                //Toast .makeText(applicationContext, "API válasz érkezett.", Toast.LENGTH_SHORT).show()

                val bookItem = response.body()?.items?.firstOrNull()
                val info = bookItem?.volumeInfo

                // Ha talált könyvet, kivesszük az adatokat (ha hiányzik, alapértelmezett értéket adunk)
                if (info != null) {
                    val title = info.title ?: getString(R.string.unknown_title)
                    val authors = info.authors?.joinToString(", ") ?: "Ismeretlen szerző"
                    val date = info.date ?: ""
                    val image = info.image ?: ""
                    val category = info.category ?: ""
                    val language = info.language ?: ""
                    val type = info.type ?: ""
                    val location = info.location ?: ""
                    val condition = info.condition ?: ""
                    val status = info.status ?: ""

                    val book = Book(
                        title = title,
                        author = authors,
                        date = date,
                        image = image,
                        category = category,
                        language = language,
                        type = type,
                        location = location,
                        condition = condition,
                        status = status,
                        isbn = isbn
                    )

                    val bookViewModel = BookViewModel(application)

                    // Coroutine-t használunk háttérművelethez (pl. DB lekérdezés)
                    CoroutineScope(Dispatchers.IO).launch {
                        val existingBook =
                            bookViewModel.findBookByIsbn(isbn)

                        if (existingBook == null) {
                            // Ha nem létezik, elmentjük
                            bookViewModel.insert(book)
                            withContext(Dispatchers.Main) {
                                //Log.d("AddBookActivity", "Könyv mentése sikeres: $title")
                                Toast.makeText(
                                    applicationContext,
                                    getString(R.string.book_saved_successfully),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            // Ha már létezik, figyelmeztetjük a felhasználót
                            withContext(Dispatchers.Main) {
                                //Log.d("AddBookActivity", "Könyv már létezik az adatbázisban.")
                                Toast.makeText(
                                    applicationContext,
                                    getString(R.string.book_already_exists),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                } else {
                    // Nem talált adatot az adott ISBN-re
                    //Log.e("AddBookActivity", "Nem találtunk adatokat ezzel az ISBN-nel: $isbn")
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.no_book_data_found),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<GoogleBooksResponse>, t: Throwable) {
                // API hiba kezelése
                //Log.e("AddBookActivity", "API hiba: ${t.message}")
                Toast.makeText(
                    applicationContext,
                    getString(R.string.book_fetch_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    // Bemeneti fájl URI-t átmásol az app belső tárhelyére, és visszaadja az új URI-t
    private fun copyUriToInternalStorage(uri: Uri): Uri? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val fileName = "copied_image_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)

            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output) // A tényleges másolás
                }
            }

            //Log.d("AddBookActivity", "Image copied to internal storage: ${file.absolutePath}")
            Uri.fromFile(file) // Visszatérünk az új fájl URI-jával
        } catch (e: Exception) {
            //Log.e("AddBookActivity", "Failed to copy image: ${e.message}")
            null // Hiba esetén null-t ad vissza
        }
    }

    // Helper metódusok a rádiógombok és a spinner kezeléséhez
    private fun setRadioButtonValue(radioGroup: RadioGroup, value: String?) {
        value?.let {
            for (i in 0 until radioGroup.childCount) {
                val radioButton = radioGroup.getChildAt(i) as RadioButton
                if (radioButton.text.toString() == value) {
                    radioButton.isChecked = true
                    break
                }
            }
        }
    }

    // Beállítja egy Spinner aktuális kiválasztott elemét a megadott érték alapján
    private fun setSpinnerSelection(spinner: Spinner, value: String?) {
        // Ha a value nem null, akkor végrehajtja a benne lévő kódblokkot
        value?.let { selectedValue ->
            // Megpróbálja az adaptert átalakítani ArrayAdapter<String> típusra
            val adapter = spinner.adapter as? ArrayAdapter<String>
            // Ha az adapter nem null és megfelelő típusú
            adapter?.let {
                // Lekéri a value-hoz tartozó pozíciót az adapterből
                val position = it.getPosition(selectedValue)
                // Beállítja a Spinner aktuálisan kiválasztott elemét erre a pozícióra
                spinner.setSelection(position)
            }
        }
    }
}
