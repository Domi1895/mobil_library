package com.example.library.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.library.adapter.BookAdapter
import com.example.library.viewmodel.BookViewModel
import com.example.library.R

class ViewFragment : Fragment() {

    private lateinit var bookRecyclerView: RecyclerView
    private lateinit var viewTypeRadioGroup: RadioGroup
    private val bookAdapter = BookAdapter()
    private lateinit var bookViewModel: BookViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // A fragment nézetének betöltése és View objektummá alakítása a hozzárendelt layout alapján
        return inflater.inflate(R.layout.fragment_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI elemek inicializálása
        bookRecyclerView = view.findViewById(R.id.book_recycler_view)
        viewTypeRadioGroup = view.findViewById(R.id.view_type_radio_group)

        // Itt állítjuk be egyszer az adaptert
        bookRecyclerView.adapter = bookAdapter

        // Alapértelmezett nézet: lista
        setupRecyclerViewLayout(LinearLayoutManager(requireContext()))

        // ViewModel inicializálása
        bookViewModel = ViewModelProvider(this)[BookViewModel::class.java]

        // Adatok figyelése és adapter frissítése
        bookViewModel.getAllBooks().observe(viewLifecycleOwner) { books ->
            bookAdapter.setBooks(books)
        }

        // Nézetváltás kezelése (Lista vagy Grid nézet)
        viewTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.view_type_list -> setupRecyclerViewLayout(LinearLayoutManager(requireContext()))
                R.id.view_type_grid -> setupRecyclerViewLayout(GridLayoutManager(requireContext(), 2))
            }
        }
    }

    // A RecyclerView layout beállítása
    private fun setupRecyclerViewLayout(layoutManager: RecyclerView.LayoutManager) {
        bookRecyclerView.layoutManager = layoutManager
    }
}
