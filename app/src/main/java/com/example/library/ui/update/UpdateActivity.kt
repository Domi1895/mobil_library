package com.example.library.ui.update

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.library.R
import com.example.library.ui.addbook.AddBookActivity

class UpdateActivity : AppCompatActivity() {

    private lateinit var updateLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Launcher regisztrálása
        updateLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            if (result.resultCode == RESULT_OK && data != null) {
                val updatedId = data.getIntExtra("id", -1)
                val updatedTitle = data.getStringExtra("title")
                val updatedAuthor = data.getStringExtra("author")
                val updatedDate = data.getStringExtra("date")
                val updatedImage = data.getStringExtra("image")
                val updatedCategory = data.getStringExtra("category")
                val updatedLanguage = data.getStringExtra("language")
                val updatedType = data.getStringExtra("type")
                val updatedLocation = data.getStringExtra("location")
                val updatedCondition = data.getStringExtra("condition")
                val updatedStatus = data.getStringExtra("status")
                val updatedIsbn = data.getStringExtra("isbn")

                if (updatedId == -1) {
                    Toast.makeText(this, getString(R.string.error_no_id), Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }

                val resultIntent = Intent().apply {
                    putExtra("id", updatedId)
                    putExtra("title", updatedTitle)
                    putExtra("author", updatedAuthor)
                    putExtra("date", updatedDate)
                    putExtra("image", updatedImage)
                    putExtra("category", updatedCategory)
                    putExtra("language", updatedLanguage)
                    putExtra("type", updatedType)
                    putExtra("location", updatedLocation)
                    putExtra("condition", updatedCondition)
                    putExtra("status", updatedStatus)
                    putExtra("isbn", updatedIsbn)
                }

                setResult(RESULT_OK, resultIntent)
            } else {
                Log.e("UpdateActivity", "Hiba: Nincs adat vagy sikertelen eredmény.")
            }

            finish()
        }

        // 2. Intent összeállítása
        val id = intent.getIntExtra("id", -1)
        val title = intent.getStringExtra("title") ?: ""
        val author = intent.getStringExtra("author") ?: ""
        val date = intent.getStringExtra("date") ?: ""
        val image = intent.getStringExtra("image") ?: ""
        val category = intent.getStringExtra("category") ?: ""
        val language = intent.getStringExtra("language") ?: ""
        val type = intent.getStringExtra("type") ?: ""
        val location = intent.getStringExtra("location") ?: ""
        val condition = intent.getStringExtra("condition") ?: ""
        val status = intent.getStringExtra("status") ?: ""
        val isbn = intent.getStringExtra("isbn") ?: ""

        val updateIntent = Intent(this, AddBookActivity::class.java).apply {
            putExtra("id", id)
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
            putExtra("isUpdate", true)
        }

        // 3. Indítás
        updateLauncher.launch(updateIntent)
    }
}
