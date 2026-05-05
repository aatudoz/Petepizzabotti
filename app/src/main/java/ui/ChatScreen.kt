package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {

    val lazyListState = rememberLazyListState()

    // Vierittää chatin listan alas kun uusi viesti tulee
    LaunchedEffect(viewModel.viestit.size) {
        if (viewModel.viestit.isNotEmpty()) {
            lazyListState.animateScrollToItem(viewModel.viestit.size - 1)
        }
    }

    // Yläosa TODO: joku online pallo Peten nimen vieree
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Pizzeria Pete", fontWeight = FontWeight.Bold)
                        Text(
                            "Pete paikalla ",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF8B0000),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            ChatSyoteRivi(
                teksti = viewModel.syote,
                ladataan = viewModel.ladataan,
                onTekstiMuuttui = viewModel::paivitaSyote,
                onLaheta = viewModel::lahetaViesti
            )
        }
    ) { padding ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFFF8F0))
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(viewModel.viestit) { viesti ->
                ViestiKupla(viesti)
            }

            if (viewModel.ladataan) {
                item {
                    Text(
                        "Luigi miettii... tai sitten ei..",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ViestiKupla(viesti: ChatViesti) {
    val taustavari = if (viesti.onKayttajalta) Color(0xFF8B0000) else Color.White
    val tekstinVari = if (viesti.onKayttajalta) Color.White else Color.Black
    val asettelu = if (viesti.onKayttajalta) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = asettelu
    ) {
        Surface(
            color = taustavari,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = viesti.teksti,
                color = tekstinVari,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSyoteRivi(
    teksti: String,
    ladataan: Boolean,
    onTekstiMuuttui: (String) -> Unit,
    onLaheta: () -> Unit
) {
    Surface(shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = teksti,
                onValueChange = onTekstiMuuttui,
                placeholder = { Text("Kirjoita Luigille...") },
                modifier = Modifier.weight(1f),
                enabled = !ladataan,
                maxLines = 3
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = onLaheta,
                enabled = !ladataan && teksti.isNotBlank(),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFF8B0000)
                )
            ) {
                Icon(Icons.Default.Send, contentDescription = "Lähetä")
            }
        }
    }
}