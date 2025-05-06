package com.example.library.googleapi

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Singleton objektum az API klienshez
object ApiClient {

    // Az alap URL, amelyhez az endpointok csatlakoznak
    private const val BASE_URL = "https://www.googleapis.com/books/v1/"

    // Lazy inicializált Retrofit kliens, amely a Google Books API-t éri el
    val googleBooksApi: GoogleBooksApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // Alap URL beállítása
            .addConverterFactory(GsonConverterFactory.create()) // JSON konverter hozzáadása
            .build()
            .create(GoogleBooksApi::class.java) // A GoogleBooksApi interfész implementálása
    }
}
