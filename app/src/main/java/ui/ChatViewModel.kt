package ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import data.ChatPyynto
import network.RetrofitClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

// Yksittäinen viesti näkymällä
data class ChatViesti(
    val teksti: String,
    val onKayttajalta: Boolean
)

class ChatViewModel : ViewModel() {

    //Viestiketju
    val viestit = mutableStateListOf<ChatViesti>()

    //Tekstikuplat chatissa
    var syote by mutableStateOf("")
        private set

    //peten vastaus lataus

    // käyttäjä lähettäny viestin -> ladataan = true
    // pete vastannut -> ladataan = false
    var ladataan by mutableStateOf(false)
        private set

    init {
        // Alkuviestit
        viestit.add(
            ChatViesti(
                teksti = "Tervetuloa Pizzeria Petelle.",
                onKayttajalta = false
            )
        )

        // Pieni viive coroutines.delay
        viewModelScope.launch {
            delay(1500)
            viestit.add(
                ChatViesti(
                    teksti = "...mitä saisi olla?",
                    onKayttajalta = false
                )
            )
        }
    }

    fun paivitaSyote(uusi: String) {
        syote = uusi
    }

    fun lahetaViesti() {
        if (syote.isBlank() || ladataan) return

        val kayttajanViesti = syote.trim()
        viestit.add(ChatViesti(kayttajanViesti, onKayttajalta = true))
        syote = ""
        ladataan = true

        viewModelScope.launch {
            try {
                val vastaus = RetrofitClient.api.lahetaViesti(
                    ChatPyynto(teksti = kayttajanViesti)
                )

                // Lisää Peten viestit yksitellen pienellä viiveellä
                for ((index, viesti) in vastaus.viestit.withIndex()) {
                    if (index > 0) delay(700) // 0.7s tauko viestien välissä
                    viestit.add(ChatViesti(viesti, onKayttajalta = false))
                }

                if (vastaus.tallennettuId != null) {
                    delay(500)
                    viestit.add(
                        ChatViesti(
                            teksti = "✅ Tilaus #${vastaus.tallennettuId} tallennettu",
                            onKayttajalta = false
                        )
                    )
                }
            } catch (e: Exception) {
                viestit.add(ChatViesti("Virhe: ${e.message ?: "tuntematon"}", onKayttajalta = false))
            } finally {
                ladataan = false
            }
        }
    }
}