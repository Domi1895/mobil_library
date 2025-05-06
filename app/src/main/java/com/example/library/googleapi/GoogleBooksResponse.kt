package com.example.library.googleapi

// A Google Books API válaszának felső szintű adatszerkezete
data class GoogleBooksResponse(
    val items: List<BookItem>? // A válaszban található könyvek listája
)

// Egy könyvtétel a válaszban, amely a volumeInfo-t tartalmazza
data class BookItem(
    val volumeInfo: VolumeInfo // Könyv részletes adatai
)

// A könyv metaadatait tartalmazó osztály
data class VolumeInfo(
    val title: String,
    val authors: List<String>?,
    val date: String?,
    val image: String?,
    val category: String?,
    val language: String?,
    val type: String?,
    val location: String?,
    val condition: String?,
    val status: String?,
    val isbn: String?
)
