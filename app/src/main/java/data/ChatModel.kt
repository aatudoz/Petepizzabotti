package data

import com.google.gson.annotations.SerializedName

//TODO: EN TIIÄ TARVIIKO MITÄÄN LISÄTÄ

// Mitä lähetetään backendille
data class ChatPyynto(
    val teksti: String
)

// Mitä backend palauttaa
data class ChatVastaus(
    val viestit: List<String>,
    @SerializedName("tilaus_valmis")
    val tilausValmis: TilausValmis? = null,
    @SerializedName("tallennettu_id")
    val tallennettuId: Int? = null
)

// Tilauksen rakenne kun Pete tunnistaa että tilaus on valmis
data class TilausValmis(
    val tuote: String,
    val koko: String?,
    val lisatilaukset: String?,
    val hinta: String?
)

data class Tilaus(
    val id: Int,
    val tuote: String?,
    val koko: String?,
    val lisatilaukset: String?,
    val hinta: String?,
    val nimi: String?,
    @SerializedName("alkuperainen_viesti")
    val alkuperainenViesti: String?,
    @SerializedName("luotu_at")
    val luotuAt: String?
)