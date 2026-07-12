package com.example.soundplayer.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.example.soundplayer.model.Sound

@Composable
fun SoundSelectionSheet(
    sounds: List<Sound>,
    selectedSounds: Set<Sound>,
    actionText: String,
    onSoundToggle: (Sound) -> Unit,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    emptyMessage: String? = null,
    playlistName: String? = null,
    playlistNameError: String? = null,
    onPlaylistNameChange: ((String) -> Unit)? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(32.dp),
    ) {
        title?.let {
            Text(
                text = it,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (playlistName != null && onPlaylistNameChange != null) {
            OutlinedTextField(
                value = playlistName,
                onValueChange = onPlaylistNameChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(text = stringResource(id = R.string.nome_da_playlist)) },
                isError = playlistNameError != null,
                supportingText = playlistNameError?.let { error -> { Text(text = error) } },
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (sounds.isEmpty()) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = emptyMessage ?: stringResource(id = R.string.nenhuma_m_sica_para_adicionar),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                )
            }
        } else {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(300.dp),
            ) {
                items(
                    items = sounds,
                    key = { sound -> sound.idSound },
                ) { sound ->
                    SoundSelectionItem(
                        sound = sound,
                        selected = selectedSounds.contains(sound),
                        onClick = { onSoundToggle(sound) },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onAction,
            modifier = Modifier.fillMaxWidth(),
            enabled = sounds.isNotEmpty(),
        ) {
            Text(text = actionText)
        }
    }
}

@Composable
private fun SoundSelectionItem(
    sound: Sound,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SoundSelectionCover(
            uri = sound.uriMediaAlbum,
            modifier = Modifier.size(72.dp),
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
        ) {
            Text(
                text =
                    if (sound.artistName == null || sound.artistName.contains("unknown")) {
                        stringResource(id = R.string.desconhecido)
                    } else {
                        sound.artistName
                    },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = sound.title,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Checkbox(
            checked = selected,
            onCheckedChange = { onClick() },
        )
    }
}

@Composable
private fun SoundSelectionCover(
    uri: String?,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = uri,
        contentDescription = null,
        placeholder = painterResource(id = R.drawable.music_player_logo_v1),
        error = painterResource(id = R.drawable.music_player_logo_v1),
        contentScale = ContentScale.Crop,
        alpha = 0.85f,
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
    )
}
