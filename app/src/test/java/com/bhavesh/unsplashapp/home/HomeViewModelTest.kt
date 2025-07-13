package com.bhavesh.unsplashapp.home

import app.cash.turbine.test
import com.bhavesh.unsplashapp.data.model.FavoriteImage
import com.bhavesh.unsplashapp.data.model.ProfileImage
import com.bhavesh.unsplashapp.data.model.UnsplashImage
import com.bhavesh.unsplashapp.data.model.Urls
import com.bhavesh.unsplashapp.data.model.User
import com.bhavesh.unsplashapp.data.repository.ImageRepository
import com.bhavesh.unsplashapp.ui.home.HomeViewModel
import com.bhavesh.unsplashapp.ui.state.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi // For TestCoroutineDispatcher, runTest
@FlowPreview // For debounce, etc. if used directly in tests (though it's in ViewModel's impl detail)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: ImageRepository
    private lateinit var viewModel: HomeViewModel // Initialized per test

    private fun dummyImage(id: String, urls: Urls? = null, user: User? = null) = UnsplashImage(
        id = id,
        description = null,
        urls = urls ?: Urls(
            thumb = "thumbUrl",
            full = "fullUrl",
            regular = "regular"
        ),
        user = user ?: User(
            name = "Test User",
            username = "testuser",
            profile_image = ProfileImage(
                small = "small"
            )
        )
    )

    // Mock data
    private val mockImage1 = dummyImage(
        "1",
        Urls("reg1", "full1", "thumb1"),
        User("User1", "user1", ProfileImage(small = "small1"))
    )

    private val mockImage2 = dummyImage(
        "2",
        Urls("reg2", "full2", "thumb2"),
        User("User2", "user2", ProfileImage(small = "small2"))
    )

    private val mockImageList = listOf(mockImage1, mockImage2)
    private val mockFavImage1 = FavoriteImage("1", "thumb1", "User1")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()

        // Default stub for getFavoriteImages. Tests can override this if they need specific favorite behavior.
        whenever(repository.getFavoriteImages()).thenReturn(flowOf(emptyList()))
        // viewModel is NOT initialized here. It will be initialized in each test.
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init - uiState is Loading initially, then Success when images fetched`() =
        runTest(testDispatcher) {
            // Arrange
            whenever(repository.getImages(1)).thenReturn(mockImageList)
            // Initialize ViewModel for this test, after specific mocks for its init block are set.
            viewModel = HomeViewModel(repository)

            // Assert
            viewModel.uiState.test {
                assertEquals("Initial state should be Loading", UiState.Loading, awaitItem())
                assertEquals(
                    "Next state should be Success with image list",
                    UiState.Success(mockImageList),
                    awaitItem()
                )
                cancelAndConsumeRemainingEvents()
            }
            verify(repository).getImages(1) // From HomeViewModel's init -> observeImageFlow
            verify(repository).getFavoriteImages() // From HomeViewModel's init -> observeFavorites
        }

    @Test
    fun `init - uiState is Error when image fetching fails`() = runTest(testDispatcher) {
        // Arrange
        val errorMessage = "Network Error"
        whenever(repository.getImages(1)).thenThrow(RuntimeException(errorMessage))
        // Initialize ViewModel for this test
        viewModel = HomeViewModel(repository)

        // Assert
        viewModel.uiState.test {
            assertEquals("Initial state should be Loading", UiState.Loading, awaitItem())
            assertEquals("Next state should be Error", UiState.Error(errorMessage), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
        verify(repository, times(1)).getImages(1)
        verify(repository, times(1)).getFavoriteImages()
    }

    @Test
    fun `init - observes favorites and updates favoriteIds`() = runTest(testDispatcher) {
        // Arrange
        val initialFavList = listOf(mockFavImage1)
        val favoritesFlow = MutableStateFlow(initialFavList)
        whenever(repository.getFavoriteImages()).thenReturn(favoritesFlow)
        // Stub for getImages(1) which is called during init, to prevent interference or ensure predictable UiState
        whenever(repository.getImages(1)).thenReturn(emptyList())
        // Initialize ViewModel for this test
        viewModel = HomeViewModel(repository)

        // Assert
        viewModel.favoriteIds.test {
            // The initial value in _favoriteIds is emptySet(), then the flow from repository is collected.
            assertEquals(
                "Initial favoriteIds should be empty before collection",
                emptySet<String>(),
                awaitItem()
            )
            assertEquals("FavoriteIds should be updated from repository", setOf("1"), awaitItem())

            // Simulate a change in favorites from the repository
            favoritesFlow.value = emptyList()
            assertEquals("FavoriteIds should reflect removal", emptySet<String>(), awaitItem())

            favoritesFlow.value = listOf(mockFavImage1, FavoriteImage("2", "thumb2", "User2"))
            assertEquals("FavoriteIds should reflect additions", setOf("1", "2"), awaitItem())

            cancelAndConsumeRemainingEvents()
        }
        verify(repository).getFavoriteImages()
    }


    @Test
    fun `onSearchQueryChanged - updates uiState with search results`() = runTest(testDispatcher) {
        // Arrange
        val query = "cats"
        val searchResults = listOf(mockImage1.copy(id = "search1"))

        // Mock initial image load to avoid interference and have a clear starting UiState
        whenever(repository.getImages(1)).thenReturn(emptyList())
        whenever(repository.searchImages(query, 1)).thenReturn(searchResults)

        viewModel = HomeViewModel(repository) // Initialize ViewModel

        viewModel.uiState.test {
            assertEquals(UiState.Loading, awaitItem()) // From init
            assertEquals(UiState.Success(emptyList<List<UnsplashImage>>()), awaitItem()) // From init getImages

            // Act
            viewModel.onSearchQueryChanged(query)
            testDispatcher.scheduler.advanceTimeBy(500) // Advance time for debounce

            // Assert search loading and results
            assertEquals("Should be Loading state for search", UiState.Loading, awaitItem())
            assertEquals(
                "Should be Success state with search results",
                UiState.Success(searchResults),
                awaitItem()
            )

            cancelAndConsumeRemainingEvents()
        }
        verify(repository).getImages(1) // From init
        verify(repository).searchImages(query, 1)
    }

    @Test
    fun `loadNextPage - fetches next page of general images when no search query`() =
        runTest(testDispatcher) {
            // Arrange
            val page1Images = listOf(mockImage1)
            val page2Images = listOf(mockImage2)
            whenever(repository.getImages(1)).thenReturn(page1Images)
            whenever(repository.getImages(2)).thenReturn(page2Images)

            viewModel = HomeViewModel(repository)

            viewModel.uiState.test {
                assertEquals(UiState.Loading, awaitItem()) // Initial load
                assertEquals(UiState.Success(page1Images), awaitItem()) // Page 1

                // Act
                viewModel.loadNextPage()
                testDispatcher.scheduler.advanceUntilIdle() // Ensure coroutines complete

                // Assert: fetchImagesFlow for page > 1 appends, doesn't emit Loading
                assertEquals(UiState.Success(page1Images + page2Images), awaitItem())

                cancelAndConsumeRemainingEvents()
            }
            verify(repository).getImages(1)
            verify(repository).getImages(2)
        }

    @Test
    fun `loadNextPage - fetches next page of search results when search query active`() =
        runTest(testDispatcher) {
            // Arrange
            val query = "nature"
            val searchPage1 = listOf(mockImage1.copy(id = "s1"))
            val searchPage2 = listOf(mockImage2.copy(id = "s2"))

            whenever(repository.getImages(1)).thenReturn(emptyList()) // For init
            whenever(repository.searchImages(query, 1)).thenReturn(searchPage1)
            whenever(repository.searchImages(query, 2)).thenReturn(searchPage2)

            viewModel = HomeViewModel(repository)

            viewModel.uiState.test {
                assertEquals(UiState.Loading, awaitItem()) // init loading
                assertEquals(UiState.Success(emptyList<List<UnsplashImage>>()), awaitItem()) // init success (empty)

                viewModel.onSearchQueryChanged(query)
                testDispatcher.scheduler.advanceTimeBy(500) // debounce

                assertEquals(UiState.Loading, awaitItem()) // search page 1 loading
                assertEquals(UiState.Success(searchPage1), awaitItem()) // search page 1 success

                // Act
                viewModel.loadNextPage()
                testDispatcher.scheduler.advanceUntilIdle()

                // Assert
                assertEquals(UiState.Success(searchPage1 + searchPage2), awaitItem())

                cancelAndConsumeRemainingEvents()
            }
            verify(repository).getImages(1) // From init
            verify(repository).searchImages(query, 1)
            verify(repository).searchImages(query, 2)
        }

    @Test
    fun `toggleFavorite - adds to favorites if not already favorite`() = runTest(testDispatcher) {
        // Arrange
        val favoritesFlow = MutableStateFlow<List<FavoriteImage>>(emptyList())
        whenever(repository.getFavoriteImages()).thenReturn(favoritesFlow)
        whenever(repository.getImages(1)).thenReturn(emptyList()) // For init

        viewModel = HomeViewModel(repository)

        viewModel.favoriteIds.test {
            assertEquals("Initial favorites should be empty", emptySet<String>(), awaitItem())

            // Act
            viewModel.toggleFavorite(mockImage1)
            testDispatcher.scheduler.advanceUntilIdle() // Allow toggleFavorite coroutine and collection to run

            // Assert repository interaction
            verify(repository).addToFavorites(
                FavoriteImage(
                    mockImage1.id,
                    mockImage1.urls.thumb,
                    mockImage1.user.name
                )
            )

            // Simulate repository update and flow emission
            favoritesFlow.value = listOf(mockFavImage1)
            assertEquals(
                "Favorite IDs should contain the added image's ID",
                setOf(mockImage1.id),
                awaitItem()
            )

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `search flow - handles emissions correctly with debounce and distinctUntilChanged`() =
        runTest(testDispatcher) {
            // Arrange
            val query1 = "trees"
            val query2 = "mountains"
            val results1 = listOf(mockImage1.copy(id = "tree1"))
            val results2 = listOf(mockImage2.copy(id = "mountain1"))

            whenever(repository.getImages(1)).thenReturn(emptyList()) // For init
            whenever(repository.searchImages(query1, 1)).thenReturn(results1)
            whenever(repository.searchImages(query2, 1)).thenReturn(results2)

            viewModel = HomeViewModel(repository)

            viewModel.uiState.test {
                assertEquals(UiState.Loading, awaitItem()) // Init loading
                assertEquals(UiState.Success(emptyList<List<UnsplashImage>>()), awaitItem()) // Init success

                // Search 1 (with debounce and distinctUntilChanged check)
                viewModel.onSearchQueryChanged(query1)
                testDispatcher.scheduler.advanceTimeBy(300) // Less than debounce, no emission yet for search
                // No new UI state expected here related to query1 yet

                viewModel.onSearchQueryChanged(query1) // Same query, should be ignored by distinctUntilChanged if debounce hadn't passed
                testDispatcher.scheduler.advanceTimeBy(200) // Total 500ms for query1, debounce passes
                assertEquals("Loading for query1", UiState.Loading, awaitItem())
                assertEquals("Success for query1", UiState.Success(results1), awaitItem())

                // Search 2
                viewModel.onSearchQueryChanged(query2)
                testDispatcher.scheduler.advanceTimeBy(500) // Pass debounce for query2
                assertEquals("Loading for query2", UiState.Loading, awaitItem())
                assertEquals("Success for query2", UiState.Success(results2), awaitItem())

                cancelAndConsumeRemainingEvents()
            }
            verify(repository, times(1)).searchImages(
                query1,
                1
            ) // distinctUntilChanged should ensure only one call for effective query1
            verify(repository, times(1)).searchImages(query2, 1)
        }

    @Test
    fun `WorkspaceImagesFlow - emits Loading then Error on repository failure during pagination for general images`() =
        runTest(testDispatcher) {
            // Arrange
            val page1Images = listOf(mockImage1)
            val errorMessage = "Failed to load page 2"
            whenever(repository.getImages(1)).thenReturn(page1Images)
            whenever(repository.getImages(2)).thenThrow(RuntimeException(errorMessage))

            viewModel = HomeViewModel(repository)

            viewModel.uiState.test {
                assertEquals(UiState.Loading, awaitItem()) // Initial load
                assertEquals(
                    UiState.Success(page1Images),
                    awaitItem()
                ) // Page 1 loaded successfully

                // Act: Try to load next page which will fail
                viewModel.loadNextPage()
                testDispatcher.scheduler.advanceUntilIdle()

                // Assert: fetchImagesFlow emits Error. The current success state will be replaced by error.
                assertEquals(UiState.Error(errorMessage), awaitItem())

                cancelAndConsumeRemainingEvents()
            }
            verify(repository).getImages(1)
            verify(repository).getImages(2)
        }

    @Test
    fun `WorkspaceImagesFlow - emits Loading then Error on repository failure during pagination for search results`() =
        runTest(testDispatcher) {
            // Arrange
            val query = "test"
            val searchPage1Images = listOf(mockImage1.copy(id = "s1"))
            val errorMessage = "Failed to load search page 2"

            whenever(repository.getImages(1)).thenReturn(emptyList()) // For init
            whenever(repository.searchImages(query, 1)).thenReturn(searchPage1Images)
            whenever(repository.searchImages(query, 2)).thenThrow(RuntimeException(errorMessage))

            viewModel = HomeViewModel(repository)

            viewModel.uiState.test {
                awaitItem() // Init Loading
                awaitItem() // Init Success (empty)

                viewModel.onSearchQueryChanged(query)
                testDispatcher.scheduler.advanceTimeBy(500) // debounce

                awaitItem() // Search page 1 Loading
                assertEquals(
                    UiState.Success(searchPage1Images),
                    awaitItem()
                ) // Search page 1 Success

                // Act: Try to load next search page which will fail
                viewModel.loadNextPage()
                testDispatcher.scheduler.advanceUntilIdle()

                // Assert
                assertEquals(UiState.Error(errorMessage), awaitItem())
                cancelAndConsumeRemainingEvents()
            }
            verify(repository).searchImages(query, 1)
            verify(repository).searchImages(query, 2)
        }
}
