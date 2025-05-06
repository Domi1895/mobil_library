package com.example.library.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.library.R
import com.example.library.db.Book

class BookAdapter : RecyclerView.Adapter<BookAdapter.BookHolder>() {

    // A megjelenítendő könyvek listája
    private var books: List<Book> = emptyList()

    // Elemre kattintáskor meghívandó függvény
    private var onItemClickListener: ((Book) -> Unit)? = null

    // ViewHolder létrehozása - meghívódik, amikor egy új nézetet kell létrehozni
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.book, parent, false)
        return BookHolder(view)
    }

    // Adatok kötése a ViewHolderhez - meghívódik, amikor egy nézetet újrahasznosítunk és adatot kell hozzárendelni
    override fun onBindViewHolder(holder: BookHolder, position: Int) {
        val currentBook = books[position]
        holder.textViewTitle.text = currentBook.title
        holder.textViewAuthor.text = currentBook.author

        // Kattintás figyelése az adott elemre
        holder.itemView.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onItemClickListener?.invoke(books[pos])
            }
        }
    }

    // A lista méretének visszaadása - hány elemet jelenítsen meg a RecyclerView
    override fun getItemCount(): Int {
        return books.size
    }

    // Könyvek listájának frissítése és az adatok újrarajzolása
    fun setBooks(newBooks: List<Book>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = books.size
            override fun getNewListSize() = newBooks.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return books[oldItemPosition].id == newBooks[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return books[oldItemPosition] == newBooks[newItemPosition]
            }
        }

        val diffResult = DiffUtil.calculateDiff(diffCallback)
        books = newBooks
        diffResult.dispatchUpdatesTo(this)
    }

    // Egy adott pozíción lévő könyv visszaadása
    fun getBooks(position: Int): Book {
        return books[position]
    }

    // Kattintásfigyelő beállítása
    fun setOnItemClickListener(listener: (Book) -> Unit) {
        onItemClickListener = listener
    }

    // ViewHolder osztály - egy könyvelem nézetének referenciáit tartalmazza
    inner class BookHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTitle: TextView = itemView.findViewById(R.id.textViewTitle)
        val textViewAuthor: TextView = itemView.findViewById(R.id.textViewAuthor)
    }
}
