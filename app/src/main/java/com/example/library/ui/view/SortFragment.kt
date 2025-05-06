package com.example.library.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.library.db.Book
import com.example.library.viewmodel.BookViewModel
import com.example.library.ui.main.MainActivity
import com.example.library.R

class SortFragment : Fragment() {

    private lateinit var bookViewModel: BookViewModel
    private var currentBooks: List<Book> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // A fragment nézetének betöltése és View objektummá alakítása - fragment megjelenítése
        return inflater.inflate(R.layout.fragment_sort, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        // ViewModel példányosítása
        bookViewModel = ViewModelProvider(requireActivity())[BookViewModel::class.java]

        // Könyvek megfigyelése és eltárolása
        bookViewModel.books.observe(viewLifecycleOwner) { books ->
            currentBooks = books ?: emptyList()
        }

        // Rendezési opciók kezelése
        val sortRadioGroup: RadioGroup = view.findViewById(R.id.sort_radio_group)

        // Figyeljük, hogy melyik opciót választja a felhasználó
        sortRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedOption = when (checkedId) {
                R.id.sort_title_az -> getString(R.string.sort_title_az)
                R.id.sort_title_za -> getString(R.string.sort_title_za)
                R.id.sort_author_az -> getString(R.string.sort_author_az)
                R.id.sort_author_za -> getString(R.string.sort_author_za)
                R.id.sort_date_ascending -> getString(R.string.sort_date_ascending)
                R.id.sort_date_descending -> getString(R.string.sort_date_descending)
                else -> getString(R.string.sort_none_selected)
            }

            // Toast üzenet a választás megjelenítéséhez (opcionálisan itt hívhatod meg a rendezési logikát)
            Toast.makeText(context, getString(R.string.toast_sort_selected, selectedOption), Toast.LENGTH_SHORT).show()
        }

        // Gombok kezelése
        val buttonCancel: View = view.findViewById(R.id.buttonCancel)
        val buttonSave: View = view.findViewById(R.id.buttonSave)

        // Törlés gomb: visszaállítja a kiválasztást
        buttonCancel.setOnClickListener {
            sortRadioGroup.clearCheck()
            Toast.makeText(context, getString(R.string.toast_sort_cleared), Toast.LENGTH_SHORT).show()
        }

        // Mentés gomb: elindítja a rendezést a kiválasztott opció alapján
        buttonSave.setOnClickListener {
            val selectedId = sortRadioGroup.checkedRadioButtonId
            val selectedOption = when (selectedId) {
                R.id.sort_title_az -> getString(R.string.sort_title_az)
                R.id.sort_title_za -> getString(R.string.sort_title_za)
                R.id.sort_author_az -> getString(R.string.sort_author_az)
                R.id.sort_author_za -> getString(R.string.sort_author_za)
                R.id.sort_date_ascending -> getString(R.string.sort_date_ascending)
                R.id.sort_date_descending -> getString(R.string.sort_date_descending)
                else -> null
            }

            if (selectedOption != null && currentBooks.isNotEmpty()) {
                // Könyvek lekérése és rendezése
                val sortedBooks = sortBooksList(currentBooks, selectedOption)

                // Frissített könyvlista átadása a MainActivitynek
                (requireActivity() as MainActivity).runOnUiThread {
                    (requireActivity() as MainActivity).updateBookList(sortedBooks)

                    // UI visszaállítása a fő nézetre
                    parentFragmentManager.popBackStack()
                    requireActivity().findViewById<View>(R.id.recyclerView).visibility = View.VISIBLE
                    requireActivity().findViewById<View>(R.id.fragment_container).visibility = View.GONE
                }
            } else {
                // Ha nincs kiválasztva rendezés, figyelmeztetés
                Toast.makeText(context, getString(R.string.toast_sort_not_selected), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Könyvlista rendezése a kiválasztott opció alapján.
    private fun sortBooksList(books: List<Book>, sortOption: String): List<Book> {
        val context = requireContext()
        return when (sortOption) {
            context.getString(R.string.sort_title_az) -> books.sortedBy { it.title.lowercase() }
            context.getString(R.string.sort_title_za) -> books.sortedByDescending { it.title.lowercase() }
            context.getString(R.string.sort_author_az) -> books.sortedBy { it.author.lowercase() }
            context.getString(R.string.sort_author_za) -> books.sortedByDescending { it.author.lowercase() }
            context.getString(R.string.sort_date_ascending) -> books.sortedBy { it.date }
            context.getString(R.string.sort_date_descending) -> books.sortedByDescending { it.date }
            else -> books
        }
    }
}
