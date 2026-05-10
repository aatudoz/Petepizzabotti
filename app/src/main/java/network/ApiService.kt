package network

import data.ChatPyynto
import data.ChatVastaus
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import data.Tilaus

interface ApiService {
    @POST("chat")
    suspend fun lahetaViesti(@Body pyynto: ChatPyynto): ChatVastaus

    @GET("tilaukset")
    suspend fun haeTilaukset(): List<Tilaus>
}