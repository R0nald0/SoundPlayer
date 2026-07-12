package com.example.soundplayer.presentation.playing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.example.soundplayer.R
import com.example.soundplayer.commons.extension.convertMilesSecondToMinSec
import com.example.soundplayer.model.Sound
import kotlinx.coroutines.delay

@Composable
fun SoundPlayingScreen(
    sound: Sound?,
    player: Player?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val unknown = "Desconhecido"
    val album = sound?.albumName?.takeUnless { it.isBlank() || it.contains("unknown") } ?: unknown
    val artist = sound?.artistName?.takeUnless { it.isBlank() || it.contains("unknown") } ?: unknown

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF590808), Color(0xFFC93636)),
                    ),
                ),
    ) {
        IconButton(
            onClick = onBack,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp, top = 8.dp),
        ) {
            BackIcon()
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = album,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 56.dp),
                color = Color(0xFFE88F8F),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = sound?.title.orEmpty(),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = artist,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                color = Color(0xFFE88F8F),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(modifier = Modifier.weight(1f))
            AlbumCover(
                uri = sound?.uriMediaAlbum,
                modifier =
                    Modifier
                        .padding(horizontal = 24.dp)
                        .sizeIn(maxWidth = 347.dp, maxHeight = 347.dp)
                        .fillMaxWidth(0.9f),
            )
            Spacer(modifier = Modifier.weight(1f))
            PlayerControls(
                player = player,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(188.dp),
            )
        }
    }
}

@Composable
private fun BackIcon() {
    Icon(
        painter = painterResource(id = R.drawable.back_ios_24),
        contentDescription = "Voltar",
        tint = Color.White,
    )
}

@Composable
private fun AlbumCover(
    uri: String?,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = uri,
        contentDescription = null,
        placeholder = painterResource(id = R.drawable.music_player_logo_v1),
        error = painterResource(id = R.drawable.music_player_logo_v1),
        contentScale = ContentScale.Crop,
        modifier =
            modifier
                .aspectRatio(1f)
                .clip(CircleShape),
    )
}

@Composable
private fun PlayerControls(
    player: Player?,
    modifier: Modifier = Modifier,
) {
    var isPlaying by remember(player) { mutableStateOf(player?.isPlaying == true) }
    var position by remember(player) { mutableStateOf(player?.currentPosition?.coerceAtLeast(0L) ?: 0L) }
    var duration by remember(player) { mutableStateOf(player?.duration?.takeIf { it > 0 } ?: 0L) }
    var shuffleEnabled by remember(player) { mutableStateOf(player?.shuffleModeEnabled == true) }
    var repeatMode by remember(player) { mutableStateOf(player?.repeatMode ?: Player.REPEAT_MODE_OFF) }

    DisposableEffect(player) {
        if (player == null) return@DisposableEffect onDispose {}

        val listener =
            object : Player.Listener {
                override fun onIsPlayingChanged(isPlayingValue: Boolean) {
                    isPlaying = isPlayingValue
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    duration = player.duration.takeIf { it > 0 } ?: 0L
                    position = player.currentPosition.coerceAtLeast(0L)
                }

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    shuffleEnabled = shuffleModeEnabled
                }

                override fun onRepeatModeChanged(repeatModeValue: Int) {
                    repeatMode = repeatModeValue
                }
            }

        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    LaunchedEffect(player, isPlaying) {
        while (player != null) {
            position = player.currentPosition.coerceAtLeast(0L)
            duration = player.duration.takeIf { it > 0 } ?: 0L
            delay(if (isPlaying) 500L else 1_000L)
        }
    }

    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = duration.convertMilesSecondToMinSec(),
                color = Color(0xFF1B1B1B),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = " | ",
                color = Color.White.copy(alpha = 0.75f),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = position.convertMilesSecondToMinSec(),
                color = Color(0xFFFFB8B8),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Slider(
            value = position.coerceAtMost(duration).toFloat(),
            onValueChange = { player?.seekTo(it.toLong()) },
            valueRange = 0f..duration.coerceAtLeast(1L).toFloat(),
            enabled = player != null && duration > 0,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            PlayerIconButton(
                iconRes = R.drawable.ic_skip_previous_24,
                contentDescription = "Música anterior",
                enabled = player != null,
                onClick = { player?.seekToPreviousMediaItem() },
            )
            PlayerTextButton(
                text = "-10",
                enabled = player != null,
                onClick = { player?.seekTo((player.currentPosition - 10_000L).coerceAtLeast(0L)) },
            )
            PlayerIconButton(
                iconRes = if (isPlaying) R.drawable.ic_pause_24 else R.drawable.ic_play_arrow_24,
                contentDescription = if (isPlaying) "Pausar" else "Reproduzir",
                enabled = player != null,
                size = 80,
                onClick = {
                    if (player?.isPlaying == true) {
                        player.pause()
                    } else {
                        player?.play()
                    }
                },
            )
            PlayerTextButton(
                text = "+10",
                enabled = player != null,
                onClick = {
                    player?.let { activePlayer ->
                        val target =
                            (activePlayer.currentPosition + 10_000L)
                                .coerceAtMost(duration.takeIf { it > 0 } ?: Long.MAX_VALUE)
                        activePlayer.seekTo(target)
                    }
                },
            )
            PlayerIconButton(
                iconRes = R.drawable.ic_skip_next_24,
                contentDescription = "Próxima música",
                enabled = player != null,
                onClick = { player?.seekToNextMediaItem() },
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            PlayerIconButton(
                iconRes = R.drawable.shuffle_split_24,
                contentDescription = if (shuffleEnabled) "Desativar aleatório" else "Ativar aleatório",
                enabled = player != null,
                selected = shuffleEnabled,
                onClick = { player?.shuffleModeEnabled = !shuffleEnabled },
            )
            PlayerIconButton(
                iconRes =
                    if (repeatMode == Player.REPEAT_MODE_ALL) {
                        R.drawable.repeat_all_360_24
                    } else {
                        R.drawable.repeat_right_alt_24
                    },
                contentDescription = "Alternar repetição",
                enabled = player != null,
                selected = repeatMode != Player.REPEAT_MODE_OFF,
                onClick = {
                    player?.repeatMode =
                        when (repeatMode) {
                            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                            else -> Player.REPEAT_MODE_OFF
                        }
                },
            )
        }
    }
}

@Composable
private fun PlayerIconButton(
    iconRes: Int,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    size: Int = 48,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(size.dp),
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            tint = if (selected) Color(0xFFB8FFCC) else Color.White,
        )
    }
}

@Composable
private fun PlayerTextButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(48.dp),
    ) {
        Text(
            text = text,
            color = if (enabled) Color.White else Color.White.copy(alpha = 0.38f),
            fontWeight = FontWeight.Bold,
        )
    }
}
