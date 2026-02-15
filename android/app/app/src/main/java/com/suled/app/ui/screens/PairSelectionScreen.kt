package com.suled.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.suled.app.R
import com.suled.app.ui.components.PairCard
import com.suled.app.viewmodel.PairSelectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairSelectionScreen(
    tournamentId: String,
    onPairSelected: (String, String) -> Unit,
    viewModel: PairSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Load pairs for this tournament when the screen is first displayed
    LaunchedEffect(tournamentId) {
        viewModel.loadPairsForTournament(tournamentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_select_pair)) },
                actions = {
                    IconButton(onClick = { viewModel.loadPairsForTournament(tournamentId) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is com.suled.app.ui.state.PairSelectionUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                is com.suled.app.ui.state.PairSelectionUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error loading pairs",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadPairsForTournament(tournamentId) }) {
                            Text("Retry")
                        }
                    }
                }
                
                is com.suled.app.ui.state.PairSelectionUiState.Success -> {
                    if (state.pairs.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No pairs found",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Please upload a tournament file first",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.pairs) { pair ->
                                PairCard(
                                    pair = pair,
                                    onClick = {
                                        onPairSelected(pair.id, pair.displayName)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
