package com.example.soundplayer.presentation.viewmodel

import com.example.soundplayer.HelperDataTest
import com.example.soundplayer.data.entities.toSongWithPlayListDomain
import com.example.soundplayer.service.SoundDomainService
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("SearchViewModel")
class SearchViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val soundDomainService = mockk<SoundDomainService>()

    private lateinit var viewModel: SearchViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SearchViewModel(soundDomainService)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `quando SearchByTitle, entao busca apos debounce e atualiza resultados`() =
        runTest {
            val result = HelperDataTest.listSoundWIthPlayLists().first().toSongWithPlayListDomain()
            coEvery { soundDomainService.findSoundByTitle("song") } returns flowOf(result)

            viewModel.onIntent(SearchIntent.SearchByTitle("song"))
            assertThat(viewModel.uiState.value.isLoading).isTrue()

            advanceTimeBy(800)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.isLoading).isFalse()
            assertThat(viewModel.uiState.value.results).containsExactly(result)
            coVerify(exactly = 1) { soundDomainService.findSoundByTitle("song") }
        }

    @Test
    fun `dado query vazia, quando SearchByTitle, entao limpa estado sem buscar`() =
        runTest {
            viewModel.onIntent(SearchIntent.SearchByTitle(""))
            advanceUntilIdle()

            assertThat(viewModel.uiState.value).isEqualTo(SearchUiState())
            coVerify(exactly = 0) { soundDomainService.findSoundByTitle(any()) }
        }
}
