package com.example.soundplayer.service

import com.example.soundplayer.HelperDataTest
import com.example.soundplayer.data.entities.toSongWithPlayListDomain
import com.example.soundplayer.data.repository.SoundRepository
import com.example.soundplayer.model.SongWithPlayListDomain
import com.example.soundplayer.model.Sound
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("SoundDomainService")
class SoundDomainServiceTest {
    private val soundRepository: SoundRepository = mockk()
    private lateinit var soundDomainService: SoundDomainService

    @BeforeEach
    fun setUp() {
        soundDomainService = SoundDomainService(soundRepository)
    }

    @Nested
    @DisplayName("saveSounds")
    inner class SaveSoundsTest {
        @Test
        fun `dado set com sons distintos, quando saveSounds, entao salva cada som individualmente`() =
            runTest {
                val sounds = HelperDataTest.listSound()
                coEvery { soundRepository.saveSound(any()) } returns 1L

                soundDomainService.saveSounds(sounds)

                sounds.forEach { sound ->
                    coVerify(exactly = 1) { soundRepository.saveSound(sound) }
                }
            }

        @Test
        fun `dado set com 2 sons, quando saveSounds, entao retorna lista com 2 ids`() =
            runTest {
                val sounds = HelperDataTest.listSound()
                coEvery { soundRepository.saveSound(any()) } returns 1L

                val result = soundDomainService.saveSounds(sounds)

                assertThat(result).hasSize(2)
            }

        @Test
        fun `dado set vazio, quando saveSounds, entao retorna lista vazia sem chamar repositorio`() =
            runTest {
                val result = soundDomainService.saveSounds(emptySet())

                assertThat(result).isEmpty()
                coVerify(exactly = 0) { soundRepository.saveSound(any()) }
            }

        @Test
        fun `dado set com 3 sons, quando saveSounds, entao nao salva o mesmo som mais de uma vez`() =
            runTest {
                val sound1 = HelperDataTest.listSound().elementAt(0)
                val sound2 = HelperDataTest.listSound().elementAt(1)
                val sound3 =
                    Sound(
                        idSound = 3L,
                        title = "Terceira música",
                        duration = "2:00",
                        albumName = "Album",
                        artistName = "Artista",
                        path = "path3",
                        insertedDate = 111L,
                    )
                val sounds = setOf(sound1, sound2, sound3)
                coEvery { soundRepository.saveSound(any()) } returns 1L

                soundDomainService.saveSounds(sounds)

                coVerify(exactly = 1) { soundRepository.saveSound(sound1) }
                coVerify(exactly = 1) { soundRepository.saveSound(sound2) }
                coVerify(exactly = 1) { soundRepository.saveSound(sound3) }
            }
    }

    @Nested
    @DisplayName("findAllSound")
    inner class FindAllSoundTest {
        @Test
        fun `quando findAllSound, entao retorna lista de sons do repositorio`() =
            runTest {
                val expected = HelperDataTest.listSound().toList()
                coEvery { soundRepository.findAllSound() } returns expected

                val result = soundDomainService.findAllSound()

                assertThat(result).hasSize(2)
                assertThat(result.first()).isInstanceOf(Sound::class.java)
                coVerify(exactly = 1) { soundRepository.findAllSound() }
            }
    }

    @Nested
    @DisplayName("delete")
    inner class DeleteTest {
        @Test
        fun `dado um som valido, quando delete, entao remove e retorna linhas afetadas`() =
            runTest {
                val sound = HelperDataTest.listSound().first()
                coEvery { soundRepository.delete(sound) } returns 1

                val result = soundDomainService.delete(sound)

                assertThat(result).isEqualTo(1)
                coVerify(exactly = 1) { soundRepository.delete(sound) }
            }
    }

    @Nested
    @DisplayName("findSoundByTitle")
    inner class FindSoundByTitleTest {
        @Test
        fun `dado um titulo, quando findSoundByTitle, entao retorna flow com sons correspondentes`() =
            runTest {
                val flowResult =
                    flow {
                        HelperDataTest.listSoundWIthPlayLists().forEach { emit(it.toSongWithPlayListDomain()) }
                    }
                coEvery { soundRepository.findSountByTitle(any()) } returns flowResult

                val result = soundDomainService.findSoundByTitle("son").toList()

                assertThat(result).hasSize(5)
                assertThat(result.first()).isInstanceOf(SongWithPlayListDomain::class.java)
                assertThat(result.last().sound.title).isEqualTo("Song Five")
            }
    }
}
