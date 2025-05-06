package com.example.library.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book")
data class Book(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val author: String,
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
