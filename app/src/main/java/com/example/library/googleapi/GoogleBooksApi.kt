package com.example.library.googleapi

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// Interfész, amely definiálja a Google Books API hívásait Retrofit-hez
interface GoogleBooksApi {

    // GET kérés a "volumes" végpontra
    // Például: https://www.googleapis.com/books/v1/volumes?q=isbn:9780140449136
    @GET("volumes")
    fun getBookByIsbn(
        @Query("q") query: String // A lekérdezés paramétere, pl.: "isbn:9780140449136"
    ): Call<GoogleBooksResponse> // A válasz egy GoogleBooksResponse objektum lesz
}
