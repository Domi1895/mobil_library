package com.example.library.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface BookDao {

    @Insert
    fun insertBook(book: Book)

    @Update
    fun updateBook(book: Book)

    @Delete
    fun deleteBook(book: Book)

    @Query("SELECT * FROM book")
    fun getAllBooks(): LiveData<List<Book>>

    @Query("SELECT * FROM book WHERE title LIKE :query OR author LIKE :query")
    fun searchBooks(query: String): LiveData<List<Book>>

    @Query("SELECT * FROM book WHERE isbn = :isbn LIMIT 1")
    suspend fun findBookByIsbn(isbn: String): Book?

    @Query("SELECT * FROM book WHERE title = :title AND author = :author LIMIT 1")
    suspend fun getBookByTitleAndAuthor(title: String, author: String): Book?

    @Query("SELECT * FROM book WHERE title = :title AND author = :author AND id != :id LIMIT 1")
    suspend fun getBookByTitleAndAuthorExcludingId(title: String, author: String, id: Int): Book?
}
