package com.example.library.ui.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.library.viewmodel.BookViewModel
import com.example.library.ui.main.MainActivity
import com.example.library.R
import com.example.library.db.Book

class FilterFragment : Fragment() {

    private lateinit var bookViewModel: BookViewModel
    private var currentBooks: List<Book> = emptyList()

    // Fragment nézeti hierarchiájának létrehozása
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_filter, container, false)
    }

    // Nézet inicializálása és eseményfigyelők beállítása
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModel példány lekérése (Activity scope-ban, megosztható más fragmentekkel)
        bookViewModel = ViewModelProvider(requireActivity())[BookViewModel::class.java]

        // Books megfigyelése
        bookViewModel.books.observe(viewLifecycleOwner) { books ->
            currentBooks = books ?: emptyList()
        }

        // Gombok beállítása
        val saveButton: Button = view.findViewById(R.id.buttonSave)
        val cancelButton: Button = view.findViewById(R.id.buttonCancel)

        // Nyelv szerinti szűrés kezelése
        val languageRadioGroup: RadioGroup = view.findViewById(R.id.language_radio_group)
        languageRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedLanguage = when (checkedId) {
                R.id.language_hu -> getString(R.string.language_hu)
                R.id.language_en -> getString(R.string.language_en)
                R.id.language_de -> getString(R.string.language_de)
                R.id.language_jp -> getString(R.string.language_jp)
                R.id.language_cn -> getString(R.string.language_cn)
                R.id.language_sp -> getString(R.string.language_sp)
                R.id.language_sg -> getString(R.string.language_sg)
                else -> getString(R.string.none_selected)
            }
            Toast.makeText(context, getString(R.string.toast_language_selected, selectedLanguage), Toast.LENGTH_SHORT).show()
        }

        // Kategóriák szűrés kezelése
        val categoryIds = listOf(
            R.id.category_other,
            R.id.category_esoterics,
            R.id.category_healthy_lifestyle,
            R.id.category_psychology,
            R.id.category_animals,
            R.id.category_dinosaurs,
            R.id.category_hobby,
            R.id.category_weapons,
            R.id.category_history,
            R.id.category_foreign_language,
            R.id.category_language_books,
            R.id.category_travel,
            R.id.category_science
        )
        for (id in categoryIds) {
            val checkBox: CheckBox = view.findViewById(id)
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                val categoryName = checkBox.text
                val action = if (isChecked) getString(R.string.selected) else getString(R.string.deselected)
                Toast.makeText(context, getString(R.string.toast_category_action, categoryName, action), Toast.LENGTH_SHORT).show()
            }
        }

        // Állapot szerinti szűrés kezelése
        val conditionRadioGroup: RadioGroup = view.findViewById(R.id.condition_radio_group)
        conditionRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedCondition = when (checkedId) {
                R.id.condition_new -> getString(R.string.condition_new)
                R.id.condition_used -> getString(R.string.condition_used)
                else -> getString(R.string.none_selected)
            }
            Toast.makeText(context, getString(R.string.toast_condition_selected, selectedCondition), Toast.LENGTH_SHORT).show()
        }

        // Státusz szerinti szűrés kezelése
        val statusRadioGroup: RadioGroup = view.findViewById(R.id.status_radio_group)
        statusRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedStatus = when (checkedId) {
                R.id.status_read -> getString(R.string.status_read)
                R.id.status_not_read -> getString(R.string.status_not_read)
                R.id.status_in_progress -> getString(R.string.status_in_progress)
                else -> getString(R.string.none_selected)
            }
            Toast.makeText(context, getString(R.string.toast_status_selected, selectedStatus), Toast.LENGTH_SHORT).show()
        }

        // Kinél van szűrés kezelése
        val personRadioGroup: RadioGroup = view.findViewById(R.id.person_radio_group)
        personRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedPerson = when (checkedId) {
                R.id.person_m -> getString(R.string.person_m)
                R.id.person_d -> getString(R.string.person_d)
                else -> getString(R.string.none_selected)
            }
            Toast.makeText(context, getString(R.string.toast_person_selected, selectedPerson), Toast.LENGTH_SHORT).show()
        }

        // Mentés gomb megnyomására szűrés elvégzése
        saveButton.setOnClickListener {
            // Nyelv szűrési logika
            val selectedLanguage = when (languageRadioGroup.checkedRadioButtonId) {
                R.id.language_hu -> getString(R.string.language_hu)
                R.id.language_en -> getString(R.string.language_en)
                R.id.language_de -> getString(R.string.language_de)
                R.id.language_jp -> getString(R.string.language_jp)
                R.id.language_cn -> getString(R.string.language_cn)
                R.id.language_sp -> getString(R.string.language_sp)
                R.id.language_sg -> getString(R.string.language_sg)
                else -> null
            }

            // Kategória szűrési logika
            val selectedCategories = categoryIds.mapNotNull {
                val cb: CheckBox = view.findViewById(it)
                if (cb.isChecked) cb.text.toString() else null
            }

            // Állapot szűrési logika
            val selectedCondition = when (conditionRadioGroup.checkedRadioButtonId) {
                R.id.condition_new -> getString(R.string.condition_new)
                R.id.condition_used -> getString(R.string.condition_used)
                else -> null
            }

            // Status szűrési logika
            val selectedStatus = when (statusRadioGroup.checkedRadioButtonId) {
                R.id.status_read -> getString(R.string.status_read)
                R.id.status_not_read -> getString(R.string.status_not_read)
                R.id.status_in_progress -> getString(R.string.status_in_progress)
                else -> null
            }

            // Person szűrési logika
            val selectedPerson = when (personRadioGroup.checkedRadioButtonId) {
                R.id.person_m -> getString(R.string.person_m)
                R.id.person_d -> getString(R.string.person_d)
                else -> null
            }

            // Könyvek lekérdezése a ViewModel-től és szűrés a megadott feltételek alapján
            if (currentBooks.isEmpty()) {
                Toast.makeText(context, getString(R.string.no_books_found), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val filteredBooks = currentBooks.filter { book ->
                    val matchesLanguage =
                        selectedLanguage == null || book.language == selectedLanguage
                    val matchesCategory =
                        selectedCategories.isEmpty() || selectedCategories.contains(book.category)
                    val matchesCondition =
                        selectedCondition == null || book.condition == selectedCondition
                    val matchesStatus = selectedStatus == null || book.status == selectedStatus
                    val matchesPerson = selectedPerson == null || book.location == selectedPerson

                    matchesLanguage && matchesCategory && matchesCondition &&
                            matchesStatus && matchesPerson
                }

                // Logolás a szűrés előtt és után
                Log.d("FilterFragment", "Szűrés előtt könyvek: $currentBooks")
                Log.d("FilterFragment", "Szűrt könyvek: $filteredBooks")

                // Ellenőrizzük, hogy a szűrt listát megkapjuk
                if (filteredBooks.isEmpty()) {
                    Log.d("FilterFragment", "Nincsenek szűrt könyvek.")
                }

                // UI frissítése a szűrt könyvlistával
                try {
                    (requireActivity() as MainActivity).runOnUiThread {
                        (requireActivity() as MainActivity).updateBookList(filteredBooks)

                        // Visszalépés a fő képernyőre
                        parentFragmentManager.popBackStack()
                        requireActivity().findViewById<RecyclerView>(R.id.recyclerView).visibility = View.VISIBLE
                        requireActivity().findViewById<View>(R.id.fragment_container).visibility = View.GONE
                    }
                } catch (e: Exception) {
                    Log.e("FilterFragment", "Hiba az adapter frissítésénél: ${e.message}")
                    e.printStackTrace()
                }
            }

        cancelButton.setOnClickListener {
            // Visszalépés az előző képernyőre
            parentFragmentManager.popBackStack()
            requireActivity().findViewById<RecyclerView>(R.id.recyclerView).visibility = View.VISIBLE
            requireActivity().findViewById<View>(R.id.fragment_container).visibility = View.GONE
        }
    }
}
