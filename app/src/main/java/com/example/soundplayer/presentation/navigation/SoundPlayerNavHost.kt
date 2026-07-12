package com.example.soundplayer.presentation.navigation

import android.Manifest
import android.app.Activity
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.soundplayer.R
import com.example.soundplayer.commons.permission.Permission
import com.example.soundplayer.model.DataSoundPlayListToUpdate
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import com.example.soundplayer.model.SoundList
import com.example.soundplayer.presentation.components.SoundSelectionSheet
import com.example.soundplayer.presentation.main.MainScreen
import com.example.soundplayer.presentation.playing.SoundPlayingScreen
import com.example.soundplayer.presentation.playlist.SelectPlaylistDialogContent
import com.example.soundplayer.presentation.search.SearchScreen
import com.example.soundplayer.presentation.settings.SettingsScreen
import com.example.soundplayer.presentation.viewmodel.PlayListIntent
import com.example.soundplayer.presentation.viewmodel.PlayListUiEvent
import com.example.soundplayer.presentation.viewmodel.PlayListViewModel
import com.example.soundplayer.presentation.viewmodel.PreferencesIntent
import com.example.soundplayer.presentation.viewmodel.PreferencesViewModel
import com.example.soundplayer.presentation.viewmodel.SearchIntent
import com.example.soundplayer.presentation.viewmodel.SearchViewModel
import com.example.soundplayer.presentation.viewmodel.SoundIntent
import com.example.soundplayer.presentation.viewmodel.SoundUiEvent
import com.example.soundplayer.presentation.viewmodel.SoundViewModel

private enum class SoundPlayerRoute(
    val route: String,
) {
    Main("main"),
    Search("search"),
    Settings("settings"),
    Player("player"),
}

private sealed interface SoundPlayerOverlay {
    data object CreatePlaylist : SoundPlayerOverlay

    data object UpdateAllMusic : SoundPlayerOverlay

    data class SelectPlaylist(
        val sounds: SoundList,
    ) : SoundPlayerOverlay
}

@Composable
fun SoundPlayerNavHost(
    playListViewModel: PlayListViewModel,
    soundViewModel: SoundViewModel,
    preferencesViewModel: PreferencesViewModel,
    searchViewModel: SearchViewModel,
) {
    val navController = rememberNavController()
    var overlay by remember { mutableStateOf<SoundPlayerOverlay?>(null) }

    NavHost(
        navController = navController,
        startDestination = SoundPlayerRoute.Main.route,
    ) {
        composable(SoundPlayerRoute.Main.route) {
            MainRoute(
                playListViewModel = playListViewModel,
                soundViewModel = soundViewModel,
                preferencesViewModel = preferencesViewModel,
                onSearch = { navController.navigate(SoundPlayerRoute.Search.route) },
                onSettings = { navController.navigate(SoundPlayerRoute.Settings.route) },
                onCreatePlaylist = { overlay = SoundPlayerOverlay.CreatePlaylist },
                onUpdateAllMusic = { overlay = SoundPlayerOverlay.UpdateAllMusic },
                onSelectPlaylist = { sounds -> overlay = SoundPlayerOverlay.SelectPlaylist(sounds) },
                onPlayer = { navController.navigate(SoundPlayerRoute.Player.route) },
            )
        }
        composable(SoundPlayerRoute.Search.route) {
            SearchRoute(
                searchViewModel = searchViewModel,
                playListViewModel = playListViewModel,
                soundViewModel = soundViewModel,
                preferencesViewModel = preferencesViewModel,
                onBack = { navController.popBackStack() },
                onPlayer = {
                    navController.popBackStack(SoundPlayerRoute.Main.route, inclusive = false)
                    navController.navigate(SoundPlayerRoute.Player.route)
                },
            )
        }
        composable(SoundPlayerRoute.Settings.route) {
            SettingsRoute(
                preferencesViewModel = preferencesViewModel,
                onBack = { navController.popBackStack() },
            )
        }
        composable(SoundPlayerRoute.Player.route) {
            SoundPlayingScreen(
                sound =
                    soundViewModel.uiState
                        .collectAsStateWithLifecycle()
                        .value.currentSound,
                player = soundViewModel.player,
                onBack = { navController.popBackStack() },
            )
        }
    }

    SoundPlayerOverlays(
        overlay = overlay,
        playListViewModel = playListViewModel,
        onDismiss = { overlay = null },
    )
}

@Composable
private fun MainRoute(
    playListViewModel: PlayListViewModel,
    soundViewModel: SoundViewModel,
    preferencesViewModel: PreferencesViewModel,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onCreatePlaylist: () -> Unit,
    onUpdateAllMusic: () -> Unit,
    onSelectPlaylist: (SoundList) -> Unit,
    onPlayer: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as Activity
    val listPermission = rememberAudioPermissions()
    val playListState by playListViewModel.uiState.collectAsStateWithLifecycle()
    val soundState by soundViewModel.uiState.collectAsStateWithLifecycle()
    val preferencesState by preferencesViewModel.uiState.collectAsStateWithLifecycle()
    var hasPermission by remember {
        mutableStateOf(Permission.checkPermissions(activity, listPermission).isEmpty())
    }
    var selectedSounds by remember { mutableStateOf<Set<Pair<Int, Sound>>>(emptySet()) }
    var selectedPlayList by remember { mutableStateOf<PlayList?>(null) }
    val fallbackPlaylist =
        playListState.playlists.firstOrNull()?.let { playlistWithSound ->
            playlistWithSound.playList.copy(
                listSound = playlistWithSound.soundOfPlayList.toMutableSet(),
            )
        }
    val effectiveSelectedPlaylist = playListState.selectedPlayList ?: fallbackPlaylist
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            hasPermission = Permission.checkPermissions(activity, listPermission).isEmpty()
            if (hasPermission && Permission.getPermissions(permissions)) {
                playListViewModel.onIntent(PlayListIntent.LoadSoundsFromDevice)
            }
        }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            playListViewModel.onIntent(PlayListIntent.CountSounds)
            playListViewModel.onIntent(PlayListIntent.LoadPlaylists)
        }
    }

    LaunchedEffect(effectiveSelectedPlaylist?.idPlayList) {
        selectedPlayList = effectiveSelectedPlaylist
        if (playListState.selectedPlayList == null) {
            effectiveSelectedPlaylist?.idPlayList?.let { id ->
                playListViewModel.onIntent(PlayListIntent.FindPlayListById(id))
            }
        }
    }

    LaunchedEffect(preferencesState.preferences?.playlistPreferenceId) {
        preferencesState.preferences?.playlistPreferenceId?.let { id ->
            playListViewModel.onIntent(PlayListIntent.FindPlayListById(id))
        }
    }

    LaunchedEffect(soundState.currentSound?.idSound, soundState.currentPlayList?.currentMusicPosition) {
        if (soundState.currentSound != null) {
            preferencesViewModel.onIntent(
                PreferencesIntent.SaveCurrentSoundPosition(
                    soundState.currentPlayList?.currentMusicPosition ?: 0,
                ),
            )
        }
    }

    LaunchedEffect(soundState.currentPlayList?.idPlayList) {
        soundState.currentPlayList?.idPlayList?.let { id ->
            preferencesViewModel.onIntent(PreferencesIntent.SavePlaylistId(id))
        }
    }

    LaunchedEffect(Unit) {
        soundViewModel.onIntent(SoundIntent.LoadActualPlayList)
        preferencesViewModel.onIntent(PreferencesIntent.ReadTextSize)
    }

    LaunchedEffect(Unit) {
        playListViewModel.uiEvent.collect { event ->
            when (event) {
                is PlayListUiEvent.ShowError -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        soundViewModel.uiEvent.collect { event ->
            when (event) {
                is SoundUiEvent.PlaybackError -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    MainScreen(
        hasPermission = hasPermission,
        isSelectionMode = selectedSounds.isNotEmpty(),
        soundCount = playListState.soundListSize,
        showNoMusicAtPlaylist = selectedPlayList?.listSound?.isEmpty() == true,
        playlists = playListState.playlists,
        selectedPlayList = selectedPlayList,
        currentPlayList = soundState.currentPlayList,
        currentSound = soundState.currentSound,
        isPlaying = soundState.isPlaying,
        selectedSounds = selectedSounds,
        titleTextSize = preferencesState.textSize,
        onSearch = onSearch,
        onSettings = onSettings,
        onCreatePlaylist = onCreatePlaylist,
        onLoadSounds = {
            val missing = Permission.checkPermissions(activity, listPermission)
            if (missing.isNotEmpty()) {
                permissionLauncher.launch(missing.toTypedArray())
            } else {
                playListViewModel.onIntent(PlayListIntent.LoadSoundsFromDevice)
            }
        },
        onClearSelection = { selectedSounds = emptySet() },
        onUpdateSelection = {
            if (selectedSounds.isNotEmpty()) {
                onSelectPlaylist(SoundList(0, selectedSounds.toMutableSet()))
            }
        },
        onPlaylistSelected = { playlist ->
            playlist.idPlayList?.let { id -> playListViewModel.onIntent(PlayListIntent.FindPlayListById(id)) }
        },
        onPlaylistEdit = { playlist -> playListViewModel.onIntent(PlayListIntent.RenamePlayList(playlist)) },
        onPlaylistDelete = { playlist -> playListViewModel.onIntent(PlayListIntent.DeletePlayList(playlist)) },
        onAddSoundAllMusic = {
            playListViewModel.onIntent(PlayListIntent.CompareDeviceSounds(idPlayList = 1))
            onUpdateAllMusic()
        },
        onSoundClick = { position, sound ->
            if (selectedSounds.isEmpty()) {
                selectedPlayList?.let { playlist ->
                    playlist.currentMusicPosition = position
                    soundViewModel.onIntent(SoundIntent.PlayPlayList(playlist))
                    onPlayer()
                }
            } else {
                selectedSounds = toggleSelectedSound(selectedSounds, position, sound)
            }
        },
        onSoundLongClick = { position, sound ->
            if (selectedSounds.isEmpty()) {
                selectedSounds = setOf(position to sound)
            }
        },
        onSoundDelete = { position, sound ->
            val playlistId = selectedPlayList?.idPlayList ?: return@MainScreen
            playListViewModel.onIntent(
                PlayListIntent.RemoveSoundFromPlayList(
                    DataSoundPlayListToUpdate(
                        idPlayList = playlistId,
                        positionSound = listOf(position),
                        sounds = setOf(sound),
                    ),
                ),
            )
        },
    )
}

@Composable
private fun SearchRoute(
    searchViewModel: SearchViewModel,
    playListViewModel: PlayListViewModel,
    soundViewModel: SoundViewModel,
    preferencesViewModel: PreferencesViewModel,
    onBack: () -> Unit,
    onPlayer: () -> Unit,
) {
    val searchState by searchViewModel.uiState.collectAsStateWithLifecycle()
    val playListState by playListViewModel.uiState.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var chosenSound by remember { mutableStateOf<Sound?>(null) }

    LaunchedEffect(playListState.selectedPlayList?.idPlayList, chosenSound?.idSound) {
        val playlist = playListState.selectedPlayList ?: return@LaunchedEffect
        val sound = chosenSound ?: return@LaunchedEffect
        val position = findSoundPositionInPlaylist(playlist, sound)
        playlist.currentMusicPosition = position
        soundViewModel.onIntent(SoundIntent.PlayPlayList(playlist))
        playlist.idPlayList?.let { id -> preferencesViewModel.onIntent(PreferencesIntent.SavePlaylistId(id)) }
        playListViewModel.onIntent(PlayListIntent.ClearSelectedPlayList)
        chosenSound = null
        onPlayer()
    }

    SearchScreen(
        query = query,
        state = searchState,
        onQueryChange = { newQuery ->
            query = newQuery
            searchViewModel.onIntent(SearchIntent.SearchByTitle(newQuery))
        },
        onBack = onBack,
        onPlaylistSelected = { soundWithPlaylists, playlist ->
            chosenSound = soundWithPlaylists.sound
            playListViewModel.onIntent(PlayListIntent.FindPlayListById(playlist.idPlayList ?: 1))
        },
    )
}

@Composable
private fun SettingsRoute(
    preferencesViewModel: PreferencesViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val state by preferencesViewModel.uiState.collectAsStateWithLifecycle()
    val orderRestartMessage = stringResource(id = R.string.settings_order_restart_message)

    LaunchedEffect(Unit) {
        preferencesViewModel.onIntent(PreferencesIntent.ReadDarkMode)
        preferencesViewModel.onIntent(PreferencesIntent.ReadAllPreferences)
        preferencesViewModel.onIntent(PreferencesIntent.ReadTextSize)
    }

    SettingsScreen(
        state = state,
        onBack = onBack,
        onThemeSelected = { selected -> preferencesViewModel.onIntent(PreferencesIntent.SaveDarkMode(selected)) },
        onTextSizeSelected = { size -> preferencesViewModel.onIntent(PreferencesIntent.SaveTextSize(size)) },
        onOrderSelected = { selected ->
            preferencesViewModel.onIntent(PreferencesIntent.SaveOrderedSound(selected))
            Toast.makeText(context, orderRestartMessage, Toast.LENGTH_SHORT).show()
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SoundPlayerOverlays(
    overlay: SoundPlayerOverlay?,
    playListViewModel: PlayListViewModel,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val playListState by playListViewModel.uiState.collectAsStateWithLifecycle()

    when (val currentOverlay = overlay) {
        SoundPlayerOverlay.CreatePlaylist -> {
            LaunchedEffect(Unit) {
                playListViewModel.onIntent(PlayListIntent.FindAllSounds)
            }
            ModalBottomSheet(onDismissRequest = onDismiss) {
                CreatePlaylistSheet(
                    sounds = playListState.soundsFromDb.sortedBy { it.title },
                    onSave = { name, sounds ->
                        playListViewModel.onIntent(
                            PlayListIntent.SavePlayList(
                                PlayList(
                                    idPlayList = null,
                                    name = name,
                                    listSound = sounds.toMutableSet(),
                                    currentMusicPosition = 0,
                                ),
                            ),
                        )
                        onDismiss()
                    },
                )
            }
        }
        SoundPlayerOverlay.UpdateAllMusic -> {
            ModalBottomSheet(onDismissRequest = onDismiss) {
                UpdateAllMusicSheet(
                    sounds = playListState.comparedSounds.sortedBy { it.title },
                    onSave = { sounds ->
                        playListViewModel.onIntent(PlayListIntent.UpdateSoundList(sounds))
                        onDismiss()
                    },
                )
            }
        }
        is SoundPlayerOverlay.SelectPlaylist -> {
            Dialog(onDismissRequest = onDismiss) {
                SelectPlaylistDialogContent(
                    playlists = playListState.playlists,
                    isLoading = playListState.isLoading,
                    onPlaylistSelected = { playlist ->
                        playlist.idPlayList?.let { playlistId ->
                            playListViewModel.onIntent(
                                PlayListIntent.UpdateSoundAtPlaylist(
                                    DataSoundPlayListToUpdate(
                                        idPlayList = playlistId,
                                        positionSound = emptyList(),
                                        sounds =
                                            currentOverlay.sounds.listMusic
                                                .map { it.second }
                                                .toSet(),
                                    ),
                                ),
                            )
                        }
                        onDismiss()
                    },
                )
            }
        }
        null -> Unit
    }

    LaunchedEffect(Unit) {
        playListViewModel.uiEvent.collect { event ->
            when (event) {
                is PlayListUiEvent.ShowError -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
private fun CreatePlaylistSheet(
    sounds: List<Sound>,
    onSave: (String, Set<Sound>) -> Unit,
) {
    val context = LocalContext.current
    var playlistName by remember { mutableStateOf("") }
    var playlistNameError by remember { mutableStateOf<String?>(null) }
    var selectedSounds by remember { mutableStateOf<Set<Sound>>(emptySet()) }
    val requiredNameText = stringResource(id = R.string.playlist_name_required)
    val emptySelectionText = stringResource(id = R.string.playlist_empty_selection_message)

    SoundSelectionSheet(
        sounds = sounds,
        selectedSounds = selectedSounds,
        actionText = stringResource(id = R.string.criar_playlist),
        playlistName = playlistName,
        playlistNameError = playlistNameError,
        onPlaylistNameChange = { name ->
            playlistName = name
            playlistNameError = null
        },
        onSoundToggle = { sound -> selectedSounds = toggleSelectedSound(selectedSounds, sound) },
        onAction = {
            when {
                playlistName.isBlank() -> playlistNameError = requiredNameText
                selectedSounds.isEmpty() -> Toast.makeText(context, emptySelectionText, Toast.LENGTH_SHORT).show()
                else -> onSave(playlistName, selectedSounds)
            }
        },
    )
}

@Composable
private fun UpdateAllMusicSheet(
    sounds: List<Sound>,
    onSave: (Set<Sound>) -> Unit,
) {
    var selectedSounds by remember { mutableStateOf<Set<Sound>>(emptySet()) }

    SoundSelectionSheet(
        title = stringResource(id = R.string.select_main_playlist_songs),
        sounds = sounds,
        selectedSounds = selectedSounds,
        actionText = stringResource(id = R.string.add_to_playlist),
        emptyMessage = stringResource(id = R.string.no_songs_to_add),
        onSoundToggle = { sound -> selectedSounds = toggleSelectedSound(selectedSounds, sound) },
        onAction = {
            if (selectedSounds.isNotEmpty()) {
                onSave(selectedSounds)
            }
        },
    )
}

@Composable
private fun rememberAudioPermissions(): Set<String> =
    remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            setOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK,
            )
        } else {
            setOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            )
        }
    }

private fun toggleSelectedSound(
    selectedSounds: Set<Pair<Int, Sound>>,
    position: Int,
    sound: Sound,
): Set<Pair<Int, Sound>> {
    val pair = position to sound
    return if (selectedSounds.contains(pair)) {
        selectedSounds - pair
    } else {
        selectedSounds + pair
    }
}

private fun toggleSelectedSound(
    selectedSounds: Set<Sound>,
    sound: Sound,
): Set<Sound> =
    if (selectedSounds.contains(sound)) {
        selectedSounds - sound
    } else {
        selectedSounds + sound
    }

internal fun findSoundPositionInPlaylist(
    playlist: PlayList,
    sound: Sound,
): Int =
    playlist.listSound
        .indexOfFirst { it.idSound == sound.idSound }
        .takeIf { it >= 0 }
        ?: 0
