package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import data.Tilaus

@Composable
fun TilauksetScreen(viewModel: TilauksetViewModel = viewModel()) {

    LaunchedEffect(Unit) {
        viewModel.lataa()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8F0))
    ) {
        when {
            viewModel.ladataan && viewModel.tilaukset.isEmpty() -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF8B0000)
                )
            }
            viewModel.virhe != null -> {
                Text(
                    text = "Virhe: ${viewModel.virhe}",
                    color = Color.Red,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
            viewModel.tilaukset.isEmpty() -> {
                Text(
                    text = "Ei tilauksia vielä. Mene tekemään yksi!",
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.tilaukset as List<Tilaus>) { tilaus ->
                        TilausKortti(tilaus)
                    }
                }
            }
        }

        // Päivitysnappi oikeassa alanurkassa
        FloatingActionButton(
            onClick = { viewModel.lataa() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFF8B0000),
            contentColor = Color.White
        ) {
            Text("↻")
        }
    }
}
@Composable
fun TilausKortti(tilaus: Tilaus) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${tilaus.id} • ${tilaus.tuote ?: "?"}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B0000)
                )
                Text(
                    text = tilaus.hinta ?: "",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(4.dp))

            tilaus.nimi?.let {
                Text("Nimi: $it", style = MaterialTheme.typography.bodyMedium)
            }
            tilaus.koko?.let {
                Text("Koko: $it", style = MaterialTheme.typography.bodyMedium)
            }
            tilaus.lisatilaukset?.let {
                if (it.isNotBlank() && it != "null") {
                    Text("Lisät: $it", style = MaterialTheme.typography.bodyMedium)
                }
            }
            tilaus.luotuAt?.let {
                Text(
                    text = it.take(16).replace("T", " "),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TilauksetScreen(
    viewModel: TilauksetViewModel = viewModel(),
    onTakaisin: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.lataa()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tilaukset", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onTakaisin) {
                        Text("←", color = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.lataa() }) {
                        Text("↻", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF8B0000),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFFF8F0))
        ) {
            when {
                viewModel.ladataan && viewModel.tilaukset.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF8B0000)
                    )
                }
                viewModel.virhe != null -> {
                    Text(
                        text = "Virhe: ${viewModel.virhe}",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                viewModel.tilaukset.isEmpty() -> {
                    Text(
                        text = "Ei tilauksia vielä. Mene tekemään yksi!",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.tilaukset as List<Tilaus>) { tilaus ->
                            TilausKortti(tilaus)
                        }
                    }
                }
            }
        }
    }
}