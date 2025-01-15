package com.example.soundplayer.data.repository


import com.example.soundplayer.data.dao.PlayListDAO
import com.example.soundplayer.data.dao.PlaylistAndSoundCrossDao
import com.example.soundplayer.data.entities.PlayListEntity
import com.example.soundplayer.data.entities.PlayListWithSong
import com.example.soundplayer.data.entities.SoundEntity
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import com.example.soundplayer.model.toEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.util.Date

@RunWith(MockitoJUnitRunner::class)
class SoundPlayListRepositoryTest {
    @Mock
    lateinit var playListDAO: PlayListDAO

    @Mock
    lateinit var playlistAndSoundCrossDao: PlaylistAndSoundCrossDao


    private lateinit var soundPlayListRepository: SoundPlayListRepository

    private lateinit var playList: PlayList

    val soundEntityList = listOf(
        SoundEntity(
            soundId = 1L,
            title = "Song One",
            artistsName = "Artist One",
            albumName = "Album One",
            path = "/music/song_one.mp3",
            duration = "3:45",
            urlMediaImage = "https://example.com/song_one_image.jpg",
            urlAlbumImage = "https://example.com/album_one_image.jpg",
            insertedDate = Date().time
        ),
        SoundEntity(
            soundId = 2L,
            title = "Song Two",
            artistsName = "Artist Two",
            albumName = "Album Two",
            path = "/music/song_two.mp3",
            duration = "4:30",
            urlMediaImage = "https://example.com/song_two_image.jpg",
            urlAlbumImage = "https://example.com/album_two_image.jpg",
            insertedDate = Date().time
        ),
        SoundEntity(
            soundId = 3L,
            title = "Song Three",
            artistsName = "Artist Three",
            albumName = "Album Three",
            path = "/music/song_three.mp3",
            duration = "5:00",
            urlMediaImage = "https://example.com/song_three_image.jpg",
            urlAlbumImage = "https://example.com/album_three_image.jpg",
            insertedDate = Date().time
        ),
        SoundEntity(
            soundId = 4L,
            title = "Song Four",
            artistsName = "Artist Four",
            albumName = "Album Four",
            path = "/music/song_four.mp3",
            duration = "2:50",
            urlMediaImage = "https://example.com/song_four_image.jpg",
            urlAlbumImage = "https://example.com/album_four_image.jpg",
            insertedDate = Date().time
        ),
        SoundEntity(
            soundId = 5L,
            title = "Song Five",
            artistsName = "Artist Five",
            albumName = "Album Five",
            path = "/music/song_five.mp3",
            duration = "3:15",
            urlMediaImage = "https://example.com/song_five_image.jpg",
            urlAlbumImage = "https://example.com/album_five_image.jpg",
            insertedDate = Date().time
        )
    )
    private val listSount = mutableSetOf(
        Sound(
            idSound = 1,
            title = "Minha primeira música",
            duration = "3:40",
            albumName = "Nome alnbum",
            artistName = "Altor",
            path = "path",
            uriMediaAlbum = "",
            uriMedia = "",
            insertedDate = 2313231
        ),
        Sound(
            idSound = 2,
            title = "Minha segunda música",
            duration = "3:40",
            albumName = "Nome alnbum",
            artistName = "Altor",
            path = "path",
            uriMediaAlbum = "",
            uriMedia = "",
            insertedDate = 2313231
        )
    )

    @Before
    fun setUp() {
        playList = PlayList(
            idPlayList = null,
            listSound = listSount,
            name = "Teste",
            currentMusicPosition = 0,
        )

        MockitoAnnotations.openMocks(this)
        soundPlayListRepository = SoundPlayListRepository(
            playListDAO = playListDAO,
            playlistAndSoundCross = playlistAndSoundCrossDao
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test()
    fun ` findAllPlayListWithSong should return a emptyList`() = runTest {

        Mockito.`when`(playlistAndSoundCrossDao.findAllPlayListWithSong()).thenReturn(emptyList())

        val result = soundPlayListRepository.findAllPlayListWithSong()

        assertThat(result).isEmpty()

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `When findAllPlayListWithSong method is executed,it should return a list of PlaylistWithSoundDomain`() = runTest {
        val mockPlayListWithSongList = listOf(
            PlayListWithSong(playList = playList.toEntity(), soundOfPlayList = soundEntityList),
        )

        Mockito.`when`(playlistAndSoundCrossDao.findAllPlayListWithSong())
            .thenReturn(mockPlayListWithSongList)

        val result = soundPlayListRepository.findAllPlayListWithSong()

        assertThat(result).isNotEmpty()
        assertThat(result.size).isEqualTo(1)
        assertThat(result.first().playList.name).isEqualTo("Teste")
        verify(playlistAndSoundCrossDao, times(1)).findAllPlayListWithSong()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given a valid playlist,when savePlaylist method is executed,it should save a playlist and return a list of long value`() = runTest {

        Mockito.`when`(playListDAO.createPlayList(playList.toEntity())).thenReturn(1L)
        Mockito.`when`(
            playlistAndSoundCrossDao.insertPlayListAndSoundCroos(Mockito.anyList())
        ).thenReturn(listOf(1,2))

        val result = soundPlayListRepository.savePlayList(playList)

        assertThat(result).isEqualTo(listOf<Long>(1,2))
        verify(playlistAndSoundCrossDao, times(1)).insertPlayListAndSoundCroos(Mockito.anyList())
        verify(playListDAO, times(1)).createPlayList(playList.toEntity())
        verify(playlistAndSoundCrossDao, times(1)).insertPlayListAndSoundCroos(Mockito.anyList())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test(expected = RepositoryException::class)
    fun `savePlayList should launch NullPointerException when invalid id`() = runTest {

        Mockito.`when`(playListDAO.createPlayList(playList.toEntity())).thenReturn(1L)
        Mockito.`when`(
            playlistAndSoundCrossDao.insertPlayListAndSoundCroos(Mockito.anyList())
        ).thenThrow(NullPointerException())

        try {
             soundPlayListRepository.savePlayList(playList)
        }catch (e:RepositoryException){
            assertThat(e).isInstanceOf(RepositoryException::class.java)
            assertThat(e.message).isEqualTo("erro ao adicionar música na playlist,id da playlist não encontrado")
            verify(playlistAndSoundCrossDao, times(1)).insertPlayListAndSoundCroos(Mockito.anyList())
            verify(playListDAO, times(1)).createPlayList(playList.toEntity())
            throw e
        }


    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `findPlayListById should find playList by your id`()= runTest{
       val playListWithSong = PlayListWithSong(
            playList = PlayListEntity(playListId = 1, currentSoundPosition = 1, title = "Teste"),
            soundOfPlayList = soundEntityList,
        )

        Mockito.`when`(playListDAO.findPlayListById(Mockito.anyLong())).thenReturn(
            playListWithSong
        )

        val result = soundPlayListRepository.findPlayListById(1)

        assertThat(result.name).isEqualTo("Teste")
        assertThat(result.listSound).isNotEmpty()
        verify(playListDAO, times(1)).findPlayListById(Mockito.anyLong())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test(expected = RepositoryException::class)
    fun `findPlayListById should launch NullPointerException when invalid id`()= runTest{

        Mockito.`when`(playListDAO.findPlayListById(Mockito.anyLong())).thenThrow(NullPointerException())
        try {
            soundPlayListRepository.findPlayListById(1)
       }catch (e:RepositoryException){
           assertThat(e.message).isEqualTo("Erro ao bucar playList,id inválido")
           assertThat(e).isInstanceOf(RepositoryException::class.java)
           verify(playListDAO, times(1)).findPlayListById(Mockito.anyLong())
           throw e
       }

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
     fun `deletePlaylist Should delete playlist `() = runTest{
         val playListEntity =playList.toEntity().copy(playListId = 1)
         Mockito.`when`(playListDAO.deletePlayList(playList = playListEntity)).thenReturn(1)
         Mockito.`when`(playlistAndSoundCrossDao.deletePlayListAndSoundCrossByIdPlayList(1L)).thenReturn(1)

         val result  = soundPlayListRepository.deletePlaylist(playList.copy(idPlayList = 1))

         assertThat(result).isEqualTo(1)
         verify(playListDAO, times(1)).deletePlayList(playListEntity)
         verify(playlistAndSoundCrossDao, times(1)).deletePlayListAndSoundCrossByIdPlayList(1L)
     }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test(expected = RepositoryException::class)
    fun `deletePlaylist should launch NullPointerException when id notFound or invalid`()= runTest {
        val playListEntity =playList.toEntity()

        Mockito.`when`(playListDAO.deletePlayList(playList = playListEntity)).thenReturn(1)

        try {
            soundPlayListRepository.deletePlaylist(playList)
        } catch (e: RepositoryException) {

            assertThat(e.message).isEqualTo("Erro ao deletar Teste,Id da playList inválido")
            assertThat(e).isInstanceOf(RepositoryException::class.java)
            throw e
        }


    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
   fun `addSountToPlayList should add sound in playlist` ()= runTest{

       Mockito.`when`(playlistAndSoundCrossDao.insertPlayListAndSoundCroos(Mockito.anyList())).thenReturn(
            listOf(1,2)
        )

      val result   =  soundPlayListRepository.addSoundToPlayList(1,listSount)
        assertThat(result).isNotEmpty()
        verify(playlistAndSoundCrossDao, times(1)).insertPlayListAndSoundCroos(Mockito.anyList())
   }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test(expected = RepositoryException::class)
    fun `addSountToPlayList should launch NullPointerException when id is invalid` ()= runTest{

        Mockito.`when`(playlistAndSoundCrossDao.insertPlayListAndSoundCroos(Mockito.anyList())).thenThrow(NullPointerException())

        try {
             soundPlayListRepository.addSoundToPlayList(1,listSount)
        }catch ( e : RepositoryException){
            assertThat(e).isInstanceOf(RepositoryException::class.java)
            assertThat(e.message).isEqualTo("erro ao adicionar música na playlist,id da playlist não encontrado")
            verify(playlistAndSoundCrossDao, times(1)).insertPlayListAndSoundCroos(Mockito.anyList())
           throw e
        }

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `removeSoundItemFromPlayList should remove Sound in to PlayList`()= runTest {
        Mockito.`when`(playlistAndSoundCrossDao.deleteItemPlayListAndSoundCroos(Mockito.anyLong(),Mockito.anyLong())).thenReturn(1)
        val result = soundPlayListRepository.removeSoundItemFromPlayList(1, 1)

        assertThat(result).isEqualTo(1)
        verify(playlistAndSoundCrossDao, times(1)).deleteItemPlayListAndSoundCroos(Mockito.anyLong(),Mockito.anyLong())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test(expected = RepositoryException::class)
    fun `removeSoundItemFromPlayList should lauch NullPointerException when id invalid`()= runTest {
        Mockito.`when`(playlistAndSoundCrossDao.deleteItemPlayListAndSoundCroos(Mockito.anyLong(),Mockito.anyLong())).thenThrow(NullPointerException())

         try {
              soundPlayListRepository.removeSoundItemFromPlayList(1, 1)
         }catch (e: RepositoryException){
             assertThat(e).isInstanceOf(RepositoryException::class.java)
             assertThat(e.message).isEqualTo("Erro ao remover áudio da playlist")
             throw e
         }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `updateNamePlayList should update name of the playlist `()= runTest {
         Mockito.`when`(playListDAO.updateNamePlayList(Mockito.anyLong(),Mockito.anyString())).thenReturn(1)

        val result = soundPlayListRepository.updateNamePlayList(1, "Nova PlayList")
        assertThat(result).isEqualTo(1)
        verify(playListDAO, times(1)).updateNamePlayList(Mockito.anyLong(),Mockito.anyString())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test(expected = RepositoryException::class)
    fun `updateNamePlayList should launch NullPointer if id invalid`()= runTest {
        Mockito.`when`(playListDAO.updateNamePlayList(Mockito.anyLong(),Mockito.anyString())).thenThrow(NullPointerException("erro id"))

       try {
            soundPlayListRepository.updateNamePlayList(1, "Nova PlayList")
       }catch (e:RepositoryException){
           assertThat(e).isInstanceOf(RepositoryException::class.java)
           assertThat(e.message).isEqualTo("Erro ao atualizar o nome da playList")
           throw  e
       }
    }

    @After
    fun tearDown() {}

}