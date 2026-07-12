package com.example.soundplayer.presentation.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.soundplayer.R
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.SongWithPlayListDomain
import com.example.soundplayer.presentation.viewmodel.SearchUiState

@Composable
fun SearchScreen(
    query: String,
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onPlaylistSelected: (SongWithPlayListDomain, PlayList) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SearchHeader(
                query = query,
                onQueryChange = onQueryChange,
                onBack = onBack,
            )

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                when {
                    state.isLoading -> CircularProgressIndicator(modifier = Modifier.padding(top = 48.dp))
                    state.results.isEmpty() -> SearchEmptyMessage(modifier = Modifier.padding(top = 48.dp))
                    else ->
                        SearchResultList(
                            results = state.results,
                            onPlaylistSelected = onPlaylistSelected,
                        )
                }
            }
        }
    }
}

@Composable
private fun SearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onBack) {
            Text(text = "<", fontWeight = FontWeight.Bold)
        }
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            placeholder = { Text(text = stringResource(id = R.string.digite_titulo_musica)) },
        )
    }
}

@Composable
private fun SearchEmptyMessage(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = R.string.lista_vazia_busque_musica),
        modifier = modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun SearchResultList(
    results: List<SongWithPlayListDomain>,
    onPlaylistSelected: (SongWithPlayListDomain, PlayList) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(
            items = results,
            key = { item -> item.sound.idSound },
        ) { result ->
            SearchResultItem(
                result = result,
                onPlaylistSelected = { playlist -> onPlaylistSelected(result, playlist) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchResultItem(
    result: SongWithPlayListDomain,
    onPlaylistSelected: (PlayList) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SoundCover(
            uri = result.sound.uriMediaAlbum,
            modifier = Modifier.size(80.dp),
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
        ) {
            Text(
                text = result.sound.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                result.listOfPlayLists.toSet().forEach { playlist ->
                    AssistChip(
                        onClick = { onPlaylistSelected(playlist) },
                        label = { Text(text = playlist.name) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SoundCover(
    uri: String?,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = uri,
        contentDescription = null,
        placeholder = painterResource(id = R.drawable.music_player_logo_v1),
        error = painterResource(id = R.drawable.music_player_logo_v1),
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
    )
}
