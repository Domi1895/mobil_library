package com.example.library.ui.main

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.library.db.Book
import com.example.library.adapter.BookAdapter
import com.example.library.ui.addbook.BookScanner
import com.example.library.viewmodel.BookViewModel
import com.example.library.R
import com.example.library.ui.settings.BackgroundSelectorFragment
import com.example.library.ui.view.FilterFragment
import com.example.library.ui.view.SortFragment
import com.example.library.ui.view.ViewFragment
import com.example.library.ui.addbook.AddBookActivity
import com.example.library.ui.update.UpdateActivity

class MainActivity : AppCompatActivity() {

    // Adapter, BookScanner, ViewModel és ActivityResultLauncherek deklarálása
    lateinit var adapter: BookAdapter
    private lateinit var bookScanner: BookScanner
    private lateinit var bookViewModel: BookViewModel
    private lateinit var activityResultLauncherForAddBook: ActivityResultLauncher<Intent>
    private lateinit var activityResultLauncherForUpdateBook: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadSavedBackground() // Betöltjük az előzőleg beállított háttérképet

        // Beállítjuk a toolbar-t
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Nem jelenítjük meg a címet

        // Inicializáljuk a BookScanner-t
        bookScanner = BookScanner(this)

        // Beállítjuk a toolbar címére kattintást (újraindítjuk az Activity-t)
        val toolbarTitle: TextView = findViewById(R.id.toolbar_title)
        toolbarTitle.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Gombok inicializálása és eseménykezelők beállítása
        val filterButton: Button = findViewById(R.id.filter_button)
        val sortButton: Button = findViewById(R.id.sort_button)
        val viewButton: Button = findViewById(R.id.view_button)
        val backgroundButton: Button = findViewById(R.id.background_button)
        val addButton: Button = findViewById(R.id.add_button)

        // Filter gomb kattintásra: Fragment csere
        filterButton.setOnClickListener {
            replaceFragment(FilterFragment())
        }

        // Sort gomb kattintásra: Fragment csere
        sortButton.setOnClickListener {
            replaceFragment(SortFragment())
        }

        // View gomb kattintásra: Fragment csere
        viewButton.setOnClickListener {
            replaceFragment(ViewFragment())
        }

        // Add gomb kattintásra: új könyv hozzáadása
        addButton.setOnClickListener {
            val intent = Intent(this@MainActivity, AddBookActivity::class.java)
            activityResultLauncherForAddBook.launch(intent)
        }

        // Background gomb kattintásra: háttérkép beállítása
        backgroundButton.setOnClickListener {
            replaceFragment(BackgroundSelectorFragment())
        }

        // Activity eredmény kezelése (új könyv hozzáadása és frissítése)
        registerActivityForAddBook()
        registerActivityForUpdateBook()

        // RecyclerView és adapter beállítása
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = BookAdapter() // Inicializáljuk az adaptert
        recyclerView.adapter = adapter

        // ViewModel beállítása és figyelése
        bookViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[BookViewModel::class.java]

        // Megfigyeljük a könyvek változásait a ViewModel-ben, és frissítjük az UI-t
        bookViewModel.getAllBooks().observe(this) { books ->
            if (books != null) {
                adapter.setBooks(books)
                Log.d("MainActivity", "UI frissítve a könyvekkel: $books")
            } else {
                Log.d("MainActivity", "Nincs könyv adat")
            }
        }

        // Keresőmező beállítása és eseménykezelők
        val searchView: SearchView = findViewById(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    Toast.makeText(this@MainActivity, getString(R.string.search, it), Toast.LENGTH_SHORT).show()
                }
                return true
            }

            // Keresési kifejezés változtatásakor élő keresés
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    // Frissítjük a könyvek listáját
                    bookViewModel.searchBooks(it).observe(this@MainActivity) { books ->
                        adapter.setBooks(books)
                    }
                }
                return true
            }
        })

        // Swipe (balra/jobbra húzás) eseménykezelő a könyvek törlésére
        ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // A bindingAdapterPosition a pozíció lekérésére
                val position = viewHolder.bindingAdapterPosition

                // Ellenőrizzük, hogy a pozíció nem érvénytelen
                if (position != RecyclerView.NO_POSITION) {
                    val book = adapter.getBooks(position) // A pozíció alapján lekérjük a könyvet

                    // Törlés megerősítő dialógus
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle(getString(R.string.delete_confirmation_title))
                        .setMessage(getString(R.string.delete_confirmation_message))
                        .setPositiveButton(getString(R.string.delete_confirmation_positive)) { _, _ ->
                            bookViewModel.delete(book) // Könyv törlése
                            Toast.makeText(applicationContext, getString(R.string.book_deleted), Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton(getString(R.string.delete_confirmation_negative)) { dialogInterface, _ ->
                            adapter.notifyItemChanged(position) // Pozíció újratöltése
                            dialogInterface.dismiss()
                        }
                        .show()
                }
            }
        }).attachToRecyclerView(recyclerView)

        // Kattintás esemény a könyvek frissítésére
        adapter.setOnItemClickListener { book ->
            val intent = Intent(this@MainActivity, UpdateActivity::class.java)
            intent.putExtra("id", book.id)
            intent.putExtra("title", book.title)
            intent.putExtra("author", book.author)
            intent.putExtra("date", book.date)
            intent.putExtra("image", book.image)
            intent.putExtra("category", book.category)
            intent.putExtra("language", book.language)
            intent.putExtra("type", book.type)
            intent.putExtra("location", book.location)
            intent.putExtra("condition", book.condition)
            intent.putExtra("status", book.status)
            intent.putExtra("isbn", book.isbn)
            activityResultLauncherForUpdateBook.launch(intent)
        }

        // Vissza gomb kezelése (fragments visszalépése)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                    findViewById<RecyclerView>(R.id.recyclerView).visibility = View.VISIBLE
                    findViewById<View>(R.id.fragment_container).visibility = View.GONE
                } else {
                    // Ha nincs fragment a back stack-en, akkor meghívjuk a super.onBackPressed()-t
                    isEnabled = false // Deaktiváljuk a callback-et, hogy a rendszer alapértelmezett vissza gomb viselkedését vegye át
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }

        // Callback regisztrálása
        onBackPressedDispatcher.addCallback(this, callback) // Regisztráljuk a callback-ot
    }

    // Menü létrehozása
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    // Menü elemek kiválasztása
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_scan_isbn) {
            startScan() // Szkennelési funkció indítása
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // Activity eredmények kezelése
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val handled = bookScanner.handleResult(requestCode, resultCode, data) { isbn ->
            Toast.makeText(this, getString(R.string.scan_successful, isbn), Toast.LENGTH_SHORT).show()
            // ISBN alapján lehet keresni, vagy új könyvet hozzáadni
        }

        if (!handled) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // Frissíti az adaptert a szűrt könyvekkel
    fun updateBookList(filteredBooks: List<Book>) {
        adapter.setBooks(filteredBooks)
    }

    // Háttérkép beállítása
    fun setAppBackground(drawable: Drawable) {
        val backgroundContainer = findViewById<FrameLayout>(R.id.background_container)
        backgroundContainer.background = drawable
    }

    // Szkennelés indítása
    private fun startScan() {
        bookScanner.startScan()
    }

    // Fragment csere, amikor egy új fragment jelenik meg
    private fun replaceFragment(fragment: Fragment) {
        findViewById<RecyclerView>(R.id.recyclerView).visibility = View.GONE
        findViewById<View>(R.id.fragment_container).visibility = View.VISIBLE

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    // Activity regisztrálása új könyv hozzáadásához
    private fun registerActivityForAddBook() {
        activityResultLauncherForAddBook = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val title = result.data!!.getStringExtra("title")
                val author = result.data!!.getStringExtra("author")
                val date = result.data!!.getStringExtra("date")
                val image = result.data!!.getStringExtra("image")
                val category = result.data!!.getStringExtra("category")
                val language = result.data!!.getStringExtra("language")
                val type = result.data!!.getStringExtra("type")
                val location = result.data!!.getStringExtra("location")
                val condition = result.data!!.getStringExtra("condition")
                val status = result.data!!.getStringExtra("status")
                val isbn = result.data!!.getStringExtra("isbn")

                if (!title.isNullOrEmpty() && !author.isNullOrEmpty()) {
                    val book = Book(
                        title = title,
                        author = author,
                        date = date,
                        image = image,
                        category = category,
                        language = language,
                        type = type,
                        location = location,
                        condition = condition,
                        status = status,
                        isbn = isbn)
                    bookViewModel.insert(book)
                }
            }
        }
    }

    // Activity regisztrálása könyv frissítéséhez
    private fun registerActivityForUpdateBook() {
        activityResultLauncherForUpdateBook = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val data = result.data
            if (result.resultCode == RESULT_OK && data != null) {
                val id = data.getIntExtra("id", -1)
                val title = data.getStringExtra("title")
                val author = data.getStringExtra("author")
                val date = data.getStringExtra("date")
                val image = data.getStringExtra("image")
                val category = data.getStringExtra("category")
                val language = data.getStringExtra("language")
                val type = data.getStringExtra("type")
                val location = data.getStringExtra("location")
                val condition = data.getStringExtra("condition")
                val status = data.getStringExtra("status")
                val isbn = data.getStringExtra("isbn")

                Log.d("MainActivity", "Frissített könyv adat: title=$title, author=$author, id=$id, date=$date, condition=$condition, language= $language")

                if (!title.isNullOrEmpty() && !author.isNullOrEmpty() && id != -1) {
                    val updatedBook = Book(
                        id = id,
                        title = title,
                        author = author,
                        date = date,
                        image = image,
                        category = category,
                        language = language,
                        type = type,
                        location = location,
                        condition = condition,
                        status = status,
                        isbn = isbn)
                    Log.d(
                        "MainActivity",
                        "Frissítéshez kész könyv objektum: $updatedBook"
                    ) // Logolás
                    bookViewModel.update(updatedBook)
                } else {
                    Log.e(
                        "MainActivity",
                        "Hibás adatok a frissítéshez: title=$title, author=$author, id=$id, date=$date, condition=$condition, language= $language"
                    )  // Hibás adatok logolása
                }
            }
        }
    }

    // Háttérkép betöltése SharedPreferences-ből
    private fun loadSavedBackground() {
        val prefs = getSharedPreferences("wallpaper_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("wallpaper_uri", null)

        uriString?.let { uriStringValue ->
            try {
                val uri = Uri.parse(uriStringValue)
                val inputStream = contentResolver.openInputStream(uri)
                val drawable = Drawable.createFromStream(inputStream, uri.toString())
                drawable?.let { drawableValue ->
                    setAppBackground(drawableValue)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
