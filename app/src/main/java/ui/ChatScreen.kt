package ui

import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel(),
    onAvaaTilaukset: () -> Unit
) {

    val lazyListState = rememberLazyListState()

    LaunchedEffect(viewModel.viestit.size) {
        if (viewModel.viestit.isNotEmpty()) {
            lazyListState.animateScrollToItem(viewModel.viestit.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            "Pizzeria Pete",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text(
                            "Paikalla 🟢",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.alpha(0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onAvaaTilaukset) {
                        Text(
                            text = "📋",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF8B0000),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
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
                        "Pete miettii...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ViestiKupla(viesti: ChatViesti) {

    val isUser = viesti.onKayttajalta

    val taustavari = if (isUser)
        Color(0xFFB00020)
    else
        MaterialTheme.colorScheme.surfaceVariant

    val tekstinVari = if (isUser)
        Color.White
    else
        MaterialTheme.colorScheme.onSurface

    val asettelu = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
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
                modifier = Modifier.padding(10.dp)
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
    Surface(
        shadowElevation = 8.dp,
        modifier = Modifier
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            OutlinedTextField(
                value = teksti,
                onValueChange = onTekstiMuuttui,
                placeholder = { Text("Kirjoita Petelle...") },
                modifier = Modifier.weight(1f),
                enabled = !ladataan,
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = { onLaheta() }
                )
            )

            Spacer(Modifier.width(8.dp))

            FilledIconButton(
                onClick = onLaheta,
                enabled = !ladataan && teksti.isNotBlank(),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFF8B0000)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Lähetä"
                )
            }
        }
    }
}