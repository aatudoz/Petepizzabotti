package network

import data.ChatPyynto
import data.ChatVastaus
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("chat")
    suspend fun lahetaViesti(@Body pyynto: ChatPyynto): ChatVastaus
}