package com.example.library.ui.settings

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.library.R
import com.example.library.ui.main.MainActivity
import com.example.library.databinding.FragmentBackgroundSelectorBinding

// Lehetővé teszi a felhasználó számára, hogy válasszon egy háttérképet
class BackgroundSelectorFragment : Fragment() {

    private var _binding: FragmentBackgroundSelectorBinding? = null
    private val binding get() = _binding!!

    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    private var tempSelectedUri: Uri? = null // ideiglenesen eltároljuk a kiválasztott képet

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBackgroundSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializáljuk az ActivityResultLauncher-t, amely lehetővé teszi a képek kiválasztását a galériából
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                handleImageSelection(it)
            }
        }

        // Galéria megnyitása gombnyomásra
        binding.pickImageButton.setOnClickListener {
            pickImageLauncher.launch("image/*") // Kép kiválasztása
        }

        // Mentés gomb: Kép mentése a háttérbe
        binding.saveButton.setOnClickListener {
            tempSelectedUri?.let { uri ->
                saveBackground(uri)
            }
        }

        // Mégse gomb: Visszaállítja a korábban beállított háttérképet
        binding.cancelButton.setOnClickListener {
            loadSavedBackground()
            showActionButtons(false) // Gombok eltüntetése
        }

        // Betölti az eddig mentett háttérképet, ha van ilyen
        loadSavedBackground()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // A binding objektum nullázása a fragment törlésével
    }

    // A háttérkép beállítása a kiválasztott URI alapján
    private fun setBackgroundFromUri(uri: Uri, showToast: Boolean = false) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val drawable = Drawable.createFromStream(inputStream, uri.toString()) // Kép betöltése a URI-ból

            drawable?.let {
                (requireActivity() as? MainActivity)?.setAppBackground(it)

                // Mentjük a kiválasztott URI-t, ha még nem történt meg
                saveUriToPrefs(uri)

                if (showToast) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.background_set_success),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showErrorMessage()
        }
    }

    // Az előnézeti háttérkép megjelenítése, a kép kiválasztása után
    private fun previewBackground(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val drawable = Drawable.createFromStream(inputStream, uri.toString()) // Előnézeti kép betöltése

            drawable?.let {
                binding.backgroundLayout.background = it
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // A kiválasztott URI mentése a SharedPreferences-be
    private fun saveUriToPrefs(uri: Uri) {
        val prefs = requireContext().getSharedPreferences("wallpaper_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("wallpaper_uri", uri.toString()).apply() // URI mentése
    }

    // A mentett háttér betöltése a SharedPreferences-ből
    private fun loadSavedBackground() {
        val prefs = requireContext().getSharedPreferences("wallpaper_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("wallpaper_uri", null) // Előzőleg mentett URI

        uriString?.let {
            val uri = Uri.parse(it)
            setBackgroundFromUri(uri)
        }
    }

    // A mentés és mégse gombok láthatóságának beállítása
    private fun showActionButtons(visible: Boolean) {
        binding.saveButton.visibility = if (visible) View.VISIBLE else View.GONE
        binding.cancelButton.visibility = if (visible) View.VISIBLE else View.GONE
    }

    // Kép kiválasztása és engedélyek kezelése
    private fun handleImageSelection(uri: Uri) {
        try {
            // Engedélyezni próbáljuk a kép olvasását (perzisztens URI engedély)
            requireContext().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: SecurityException) {
            Log.e("BG", "Engedély mentése sikertelen", e)
        }

        tempSelectedUri = uri
        previewBackground(uri)
        showActionButtons(true)
    }

    // Kép mentése és háttérbeállítás
    private fun saveBackground(uri: Uri) {
        try {
            requireContext().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            saveUriToPrefs(uri)
            setBackgroundFromUri(uri, true)
            showActionButtons(false)
            Toast.makeText(requireContext(), getString(R.string.background_saved), Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    // Hibaüzenet megjelenítése
    private fun showErrorMessage() {
        Toast.makeText(requireContext(), getString(R.string.background_load_failed), Toast.LENGTH_SHORT).show()
    }
}
