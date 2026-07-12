package com.example.soundplayer.presentation.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.soundplayer.R
import com.example.soundplayer.commons.constants.Constants
import com.example.soundplayer.commons.extension.convertMilesSecondToMinSec
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.PlaylistWithSoundDomain
import com.example.soundplayer.model.Sound

@Composable
fun MainScreen(
    hasPermission: Boolean,
    isSelectionMode: Boolean,
    soundCount: Int,
    showNoMusicAtPlaylist: Boolean,
    playlists: List<PlaylistWithSoundDomain>,
    selectedPlayList: PlayList?,
    currentPlayList: PlayList?,
    currentSound: Sound?,
    isPlaying: Boolean,
    selectedSounds: Set<Pair<Int, Sound>>,
    titleTextSize: Float,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onCreatePlaylist: () -> Unit,
    onLoadSounds: () -> Unit,
    onClearSelection: () -> Unit,
    onUpdateSelection: () -> Unit,
    onPlaylistSelected: (PlayList) -> Unit,
    onPlaylistEdit: (PlayList) -> Unit,
    onPlaylistDelete: (PlayList) -> Unit,
    onAddSoundAllMusic: () -> Unit,
    onSoundClick: (Int, Sound) -> Unit,
    onSoundLongClick: (Int, Sound) -> Unit,
    onSoundDelete: (Int, Sound) -> Unit,
    modifier: Modifier = Modifier,
) {
    val showLibrary = hasPermission && soundCount > 0

    Surface(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (hasPermission) {
                    MainTopBar(
                        isSelectionMode = isSelectionMode,
                        onSearch = onSearch,
                        onSettings = onSettings,
                        onClearSelection = onClearSelection,
                        onUpdateSelection = onUpdateSelection,
                    )
                }

                if (!showLibrary) {
                    MainEmptyState(onLoadSounds = onLoadSounds)
                } else {
                    MainLibraryContent(
                        soundCount = soundCount,
                        showNoMusicAtPlaylist = showNoMusicAtPlaylist,
                        playlists = playlists,
                        selectedPlayList = selectedPlayList,
                        currentPlayList = currentPlayList,
                        currentSound = currentSound,
                        isPlaying = isPlaying,
                        selectedSounds = selectedSounds,
                        titleTextSize = titleTextSize,
                        onPlaylistSelected = onPlaylistSelected,
                        onPlaylistEdit = onPlaylistEdit,
                        onPlaylistDelete = onPlaylistDelete,
                        onAddSoundAllMusic = onAddSoundAllMusic,
                        onSoundClick = onSoundClick,
                        onSoundLongClick = onSoundLongClick,
                        onSoundDelete = onSoundDelete,
                    )
                }
            }

            if (hasPermission && !isSelectionMode) {
                FloatingActionButton(
                    onClick = onCreatePlaylist,
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_playlist_add_24),
                        contentDescription = stringResource(id = R.string.text_criar_playlist),
                    )
                }
            }
        }
    }
}

@Composable
private fun MainTopBar(
    isSelectionMode: Boolean,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onClearSelection: () -> Unit,
    onUpdateSelection: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .height(64.dp)
                .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.headlineSmall,
        )

        if (isSelectionMode) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClearSelection) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                        contentDescription = stringResource(id = R.string.fechar),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                IconButton(onClick = onUpdateSelection) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_save),
                        contentDescription = stringResource(id = R.string.atualizar),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onSearch) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_search),
                        contentDescription = stringResource(id = R.string.buscar),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                IconButton(onClick = onSettings) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_manage),
                        contentDescription = stringResource(id = R.string.config_short),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun MainEmptyState(onLoadSounds: () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.nenhuma_m_sica),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onLoadSounds) {
            Text(text = stringResource(id = R.string.procurar_musicas))
        }
    }
}

@Composable
private fun MainLibraryContent(
    soundCount: Int,
    showNoMusicAtPlaylist: Boolean,
    playlists: List<PlaylistWithSoundDomain>,
    selectedPlayList: PlayList?,
    currentPlayList: PlayList?,
    currentSound: Sound?,
    isPlaying: Boolean,
    selectedSounds: Set<Pair<Int, Sound>>,
    titleTextSize: Float,
    onPlaylistSelected: (PlayList) -> Unit,
    onPlaylistEdit: (PlayList) -> Unit,
    onPlaylistDelete: (PlayList) -> Unit,
    onAddSoundAllMusic: () -> Unit,
    onSoundClick: (Int, Sound) -> Unit,
    onSoundLongClick: (Int, Sound) -> Unit,
    onSoundDelete: (Int, Sound) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Text(
            text = stringResource(id = R.string.total_de_musicas, soundCount.toString()),
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge,
        )
        if (playlists.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.playlists),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge,
            )
            PlaylistRow(
                playlists = playlists,
                selectedPlayList = selectedPlayList,
                currentPlayList = currentPlayList,
                isPlaying = isPlaying,
                onPlaylistSelected = onPlaylistSelected,
                onPlaylistEdit = onPlaylistEdit,
                onPlaylistDelete = onPlaylistDelete,
                onAddSoundAllMusic = onAddSoundAllMusic,
            )
            Spacer(modifier = Modifier.height(12.dp))
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }
        Text(
            text = stringResource(id = R.string.musicas),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge,
        )
        if (showNoMusicAtPlaylist) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(id = R.string.est_playlist_esta_vazia),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        } else {
            SoundList(
                playlist = selectedPlayList,
                currentSound = currentSound,
                isPlaying = isPlaying,
                selectedSounds = selectedSounds,
                titleTextSize = titleTextSize,
                onSoundClick = onSoundClick,
                onSoundLongClick = onSoundLongClick,
                onSoundDelete = onSoundDelete,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PlaylistRow(
    playlists: List<PlaylistWithSoundDomain>,
    selectedPlayList: PlayList?,
    currentPlayList: PlayList?,
    isPlaying: Boolean,
    onPlaylistSelected: (PlayList) -> Unit,
    onPlaylistEdit: (PlayList) -> Unit,
    onPlaylistDelete: (PlayList) -> Unit,
    onAddSoundAllMusic: () -> Unit,
) {
    LazyRow(
        modifier =
            Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 88.dp)
                .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = playlists,
            key = { item -> item.playList.idPlayList ?: item.playList.name },
        ) { item ->
            PlaylistCard(
                playlist = item.playList,
                isSelected = selectedPlayList?.idPlayList == item.playList.idPlayList,
                isPlaying = isPlaying && currentPlayList?.idPlayList == item.playList.idPlayList,
                onClick = { onPlaylistSelected(item.playList) },
                onEdit = { onPlaylistEdit(item.playList) },
                onDelete = { onPlaylistDelete(item.playList) },
                onAddSoundAllMusic = onAddSoundAllMusic,
            )
        }
    }
}

@Composable
private fun PlaylistCard(
    playlist: PlayList,
    isSelected: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddSoundAllMusic: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

    Card(
        modifier =
            Modifier
                .size(width = 112.dp, height = 92.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { menuExpanded = true },
                ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(2.dp, borderColor),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = if (isPlaying) ">" else "~",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = playlist.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                if (playlist.name == Constants.ALL_MUSIC_NAME) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = R.string.adicionar_m_sica)) },
                        onClick = {
                            menuExpanded = false
                            onAddSoundAllMusic()
                        },
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = R.string.editar_nome)) },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = R.string.excluir)) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SoundList(
    playlist: PlayList?,
    currentSound: Sound?,
    isPlaying: Boolean,
    selectedSounds: Set<Pair<Int, Sound>>,
    titleTextSize: Float,
    onSoundClick: (Int, Sound) -> Unit,
    onSoundLongClick: (Int, Sound) -> Unit,
    onSoundDelete: (Int, Sound) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sounds = playlist?.listSound?.toList().orEmpty()

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
    ) {
        itemsIndexed(
            items = sounds,
            key = { _, sound -> sound.idSound },
        ) { index, sound ->
            SoundItem(
                sound = sound,
                position = index,
                isCurrent = currentSound?.idSound == sound.idSound,
                isPlaying = isPlaying && currentSound?.idSound == sound.idSound,
                isSelected = selectedSounds.contains(index to sound),
                titleTextSize = titleTextSize,
                onClick = { onSoundClick(index, sound) },
                onLongClick = { onSoundLongClick(index, sound) },
                onDelete = { onSoundDelete(index, sound) },
            )
        }
    }
}

@Composable
private fun SoundItem(
    sound: Sound,
    position: Int,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isSelected: Boolean,
    titleTextSize: Float,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val containerColor =
        if (isSelected) {
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SoundCover(uri = sound.uriMediaAlbum, modifier = Modifier.size(72.dp))
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
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = sound.title,
                    color = if (isCurrent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    fontSize = titleTextSize.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = sound.duration.toLongOrNull()?.convertMilesSecondToMinSec() ?: "00:00",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    if (isPlaying) {
                        Text(
                            text = "  ${stringResource(id = R.string.tocando)}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
            Box {
                TextButton(onClick = { menuExpanded = true }) {
                    Text(text = "...", color = MaterialTheme.colorScheme.onSurface)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = R.string.excluir)) },
                        onClick = {
                            menuExpanded = false
                            showDeleteDialog = true
                        },
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = stringResource(id = R.string.text_delete_sound, sound.title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                ) {
                    Text(text = stringResource(id = R.string.sim))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = stringResource(id = R.string.n_o))
                }
            },
        )
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
