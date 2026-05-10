package ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.Tilaus
import network.RetrofitClient
import kotlinx.coroutines.launch

class TilauksetViewModel : ViewModel() {

    val tilaukset = mutableStateListOf<Tilaus>()

    var ladataan by mutableStateOf(false)
        private set

    var virhe by mutableStateOf<String?>(null)
        private set

    fun lataa() {
        ladataan = true
        virhe = null
        viewModelScope.launch {
            try {
                val haetut = RetrofitClient.api.haeTilaukset()
                tilaukset.clear()
                tilaukset.addAll(haetut)
            } catch (e: Exception) {
                virhe = e.message ?: "Virhe ladattaessa tilauksia"
            } finally {
                ladataan = false
            }
        }
    }
}