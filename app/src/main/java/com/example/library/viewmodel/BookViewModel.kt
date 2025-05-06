package com.example.library.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.library.db.Book
import com.example.library.repository.BookRepo
import kotlinx.coroutines.launch

// ViewModel osztály, amely a könyvekkel kapcsolatos adatokat kezeli, és biztosítja azok elérhetőségét a UI számára
class BookViewModel(application: Application) : AndroidViewModel(application) {

    // A BookRepo példányosítása, amely az adatbázis műveleteket végzi
    private val repo: BookRepo = BookRepo(application)

    // Az összes könyv LiveData formában (figyelhető adat)
    val books: LiveData<List<Book>> = repo.getAllBooks()

    // Könyv beszúrása a repository-ba
    fun insert(book: Book) {
        repo.insert(book) // Könyv hozzáadása az adatbázishoz
    }

    // Könyv frissítése a repository-ban
    fun update(book: Book) {
        repo.update(book) // Könyv adatainak frissítése
    }

    // Könyv törlése a repository-ból
    fun delete(book: Book) {
        repo.delete(book) // Könyv törlése az adatbázisból
    }

    // Az összes könyv lekérése
    fun getAllBooks(): LiveData<List<Book>> {
        return books // Visszaadjuk az összes könyvet LiveData formában
    }

    // Könyvek keresése cím és/vagy szerző alapján
    fun searchBooks(query: String): LiveData<List<Book>> {
        return repo.searchBooks(query) // Keresés a repository-ban, és a talált könyvek visszaadása
    }

    // Könyv keresése ISBN alapján
    suspend fun findBookByIsbn(isbn: String): Book? {
        return repo.findBookByIsbn(isbn) // Keresés ISBN alapján a repository-ban, és a talált könyv visszaadása
    }

    // Könyv keresése cím és szerző alapján (LiveData-t ad vissza)
    fun getBookByTitleAndAuthor(title: String, author: String): LiveData<Book?> {
        val result = MutableLiveData<Book?>() // Létrehozunk egy MutableLiveData objektumot, hogy a UI figyelhesse az eredményt
        viewModelScope.launch {
            // Aszinkron módon meghívjuk a repository-t, és a választ a LiveData-ba rakjuk
            result.postValue(repo.getBookByTitleAndAuthor(title, author))
        }
        return result // Visszaadjuk a MutableLiveData-t, ami a könyvet tartalmazza (vagy null-t, ha nincs találat)
    }

    // Könyv keresése cím és szerző alapján, figyelembe véve az ID-t is
    fun getBookByTitleAndAuthorExcludingId(title: String, author: String, id: Int): LiveData<Book?> {
        val result = MutableLiveData<Book?>() // Létrehozzuk a MutableLiveData objektumot
        viewModelScope.launch {
            // Aszinkron hívás a repository-hoz, amely figyelembe veszi az ID-t
            result.postValue(repo.getBookByTitleAndAuthorExcludingId(title, author, id))
        }
        return result // Visszaadjuk a MutableLiveData-t, ami a könyvet tartalmazza
    }
}
