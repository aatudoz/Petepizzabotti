package com.example.petepizzabotti

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.petepizzabotti.ui.theme.PetepizzabottiTheme
import retrofit2.Retrofit
import ui.ChatScreen
import ui.TilauksetScreen
import androidx.compose.runtime.*


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetepizzabottiTheme {
                PaaNakyma()
                }
            }
        }
    }

@Composable
fun PaaNakyma() {
    var nakyma by remember { mutableStateOf("chat") }

    when (nakyma) {
        "chat" -> ChatScreen(
            onAvaaTilaukset = { nakyma = "tilaukset" }
        )
        "tilaukset" -> TilauksetScreen(
            onTakaisin = { nakyma = "chat" }
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PetepizzabottiTheme {
        Greeting("Android")
    }
}

