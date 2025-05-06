package com.example.library.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.library.db.Book
import com.example.library.db.BookDB
import com.example.library.db.BookDao
import java.util.concurrent.Executors

// A könyvek adatainak kezelését végző repository osztály
class BookRepo(application: Application) {

    // Adatbázis elérhetősége
    private val bookDao: BookDao
    private val executorService = Executors.newSingleThreadExecutor() // A háttérben futó szál a műveletekhez
    private val books: LiveData<List<Book>> // Minden könyv adatainak megfigyelése (LiveData)

    init {
        // A BookDB példányosítása és az adatbázis DAO-jának lekérése
        val db = BookDB.getInstance(application)
        bookDao = db.bookDao()
        books = bookDao.getAllBooks() // Az összes könyv lekérdezése a DAO-ból
    }

    // Könyv beszúrása az adatbázisba (háttérszálon)
    fun insert(book: Book) {
        executorService.execute {
            bookDao.insertBook(book) // Könyv hozzáadása az adatbázishoz
        }
    }

    // Könyv frissítése az adatbázisban (háttérszálon)
    fun update(book: Book) {
        executorService.execute {
            bookDao.updateBook(book) // Könyv adatainak frissítése
        }
    }

    // Könyv törlése az adatbázisból (háttérszálon)
    fun delete(book: Book) {
        executorService.execute {
            bookDao.deleteBook(book) // Könyv törlése
        }
    }

    // Az összes könyv lekérdezése (LiveData formájában, hogy figyelni tudjuk az adatváltozásokat)
    fun getAllBooks(): LiveData<List<Book>> {
        return books // Az összes könyv adatainak visszaadása
    }

    // Könyvek keresése cím és/vagy szerző alapján
    fun searchBooks(query: String): LiveData<List<Book>> {
        val formattedQuery = "%$query%" // A keresési kifejezés formázása
        return bookDao.searchBooks(formattedQuery) // Keresés a DAO-ban
    }

    // Könyv keresése ISBN alapján
    suspend fun findBookByIsbn(isbn: String): Book? {
        return bookDao.findBookByIsbn(isbn) // Könyv keresése az ISBN alapján
    }

    // Könyv keresése cím és szerző alapján
    suspend fun getBookByTitleAndAuthor(title: String, author: String): Book? {
        return bookDao.getBookByTitleAndAuthor(title, author) // Könyv keresése a cím és szerző alapján
    }

    // Könyv keresése cím és szerző alapján, figyelembe véve az ID-t is (ne legyen ütközés)
    suspend fun getBookByTitleAndAuthorExcludingId(title: String, author: String, id: Int): Book? {
        return bookDao.getBookByTitleAndAuthorExcludingId(title, author, id) // Könyv keresése a cím és szerző alapján, de nem ugyanazzal az ID-val
    }
}
