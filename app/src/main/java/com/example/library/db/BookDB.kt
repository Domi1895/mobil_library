package com.example.library.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.concurrent.Executors

// Room adatbázis definíció a Book entitással, 18-es verzióval.
@Database(entities = [Book::class], version = 18, exportSchema = false)
abstract class BookDB : RoomDatabase() {

    // Absztrakt DAO metódus, amelyen keresztül elérjük az adatbázis műveleteket
    abstract fun bookDao(): BookDao

    companion object {
        // Singleton példány - csak egyetlen példány létezhet az adatbázisból
        @Volatile
        private var instance: BookDB? = null

        // Callback, amely akkor fut le, amikor az adatbázis első alkalommal létrejön
        private val roomCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                // DAO elérése (ha már példányosítva lett)
                val bookDao = instance?.bookDao()

                // Háttérszálon végrehajtott műveletek (pl. előre definiált adatok beszúrása)
                val executorService = Executors.newSingleThreadExecutor()
                executorService.execute {
                    bookDao?.insertBook(
                        Book(
                            title = "Hagakure - A szamurájok kódexe",
                            author = "Jamamoto Cunetomo",
                            date = "2011",
                            image = null,
                            category = "Történelem",
                            language = "Magyar",
                            type = null,
                            location = null,
                            condition = null,
                            status = null,
                            isbn = "9786156702197"
                        )
                    )
                    bookDao?.insertBook(
                        Book(
                            title = "Lövész iskola – Lőkiképzési és lövészeti útmutató",
                            author = "Szikszay Tamás",
                            date = "2021",
                            image = null,
                            category = "Fegyverek",
                            language = "Magyar",
                            type = null,
                            location = null,
                            condition = null,
                            status = null,
                            isbn = "9786155346439"
                        )
                    )
                    bookDao?.insertBook(
                        Book(
                            title = "Vidám történetek németül 1.",
                            author = "Maklári Tamás",
                            date = "2015",
                            image = null,
                            category = "Idegennyelvű",
                            language = "Német",
                            type = null,
                            location = null,
                            condition = null,
                            status = null,
                            isbn = "9789638736253"
                        )
                    )
                }
            }
        }

        // Singleton példány elérése (vagy létrehozása, ha még nem létezik)
        fun getInstance(context: Context): BookDB {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BookDB::class.java,
                    "book_database18" // Az adatbázis fájl neve
                )
                    .fallbackToDestructiveMigration() // Ha verzióváltozás van, törli és újra létrehozza az adatbázist
                    .addCallback(roomCallback) // Létrehozáskor futó callback hozzáadása
                    .build()
                    .also { instance = it } // Az új példány eltárolása a singletonban
            }
        }
    }
}
