package org.gotson.komga.interfaces.api.rest

import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.SeriesCollection
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.domain.service.LibraryLifecycle
import org.gotson.komga.domain.service.SeriesCollectionLifecycle
import org.gotson.komga.domain.service.SeriesLifecycle
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class SeriesCollectionControllerTest(
  @Autowired private val mockMvc: MockMvc,
  @Autowired private val collectionLifecycle: SeriesCollectionLifecycle,
  @Autowired private val collectionRepository: SeriesCollectionRepository,
  @Autowired private val libraryLifecycle: LibraryLifecycle,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val seriesLifecycle: SeriesLifecycle,
  @Autowired private val seriesMetadataRepository: SeriesMetadataRepository,
) {
  private val library1 = makeLibrary("Library1", id = "1")
  private val library2 = makeLibrary("Library2", id = "2")
  private lateinit var seriesLibrary1: List<Series>
  private lateinit var seriesLibrary2: List<Series>
  private lateinit var colLib1: SeriesCollection
  private lateinit var colLib2: SeriesCollection
  private lateinit var colLibBoth: SeriesCollection

  @BeforeAll
  fun setup() {
    libraryRepository.insert(library1)
    libraryRepository.insert(library2)

    seriesLibrary1 =
      (1..5)
        .map { makeSeries("Series_$it", library1.id) }
        .map { seriesLifecycle.createSeries(it) }

    seriesLibrary2 =
      (6..10)
        .map { makeSeries("Series_$it", library2.id) }
        .map { seriesLifecycle.createSeries(it) }
  }

  @AfterAll
  fun teardown() {
    libraryRepository.findAll().forEach {
      libraryLifecycle.deleteLibrary(it)
    }
  }

  @AfterEach
  fun clear() {
    collectionRepository.deleteAll()
  }

  private fun makeCollections() {
    colLib1 =
      collectionLifecycle.addCollection(
        SeriesCollection(
          name = "Lib1",
          seriesIds = seriesLibrary1.map { it.id },
        ),
      )

    colLib2 =
      collectionLifecycle.addCollection(
        SeriesCollection(
          name = "Lib2",
          seriesIds = seriesLibrary2.map { it.id },
        ),
      )

    colLibBoth =
      collectionLifecycle.addCollection(
        SeriesCollection(
          name = "Lib1+2",
          seriesIds = (seriesLibrary1 + seriesLibrary2).map { it.id },
          ordered = true,
        ),
      )
  }

  @Nested
  inner class GetAndFilter {
    @Test
    @WithMockCustomUser
    fun `given user with access to all libraries when getting collections then get all collections`() {
      makeCollections()

      mockMvc
        .get("/api/v1/collections")
        .andExpect {
          status { isOk() }
          jsonPath("$.totalElements") { value(3) }
          jsonPath("$.content[?(@.name == 'Lib1')].filtered") { value(false) }
          jsonPath("$.content[?(@.name == 'Lib2')].filtered") { value(false) }
          jsonPath("$.content[?(@.name == 'Lib1+2')].filtered") { value(false) }
        }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = ["1"])
    fun `given user with access to a single library when getting collections then only get collections from this library`() {
      makeCollections()

      mockMvc
        .get("/api/v1/collections")
        .andExpect {
          status { isOk() }
          jsonPath("$.totalElements") { value(2) }
          jsonPath("$.content[?(@.name == 'Lib1')].filtered") { value(false) }
          jsonPath("$.content[?(@.name == 'Lib1+2')].filtered") { value(true) }
        }
    }

    @Test
    @WithMockCustomUser
    fun `given user with access to all libraries when getting single collection then it is not filtered`() {
      makeCollections()

      mockMvc
        .get("/api/v1/collections/${colLibBoth.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.seriesIds.length()") { value(10) }
          jsonPath("$.filtered") { value(false) }
        }

      mockMvc
        .get("/api/v1/collections/${colLibBoth.id}/series")
        .andExpect {
          status { isOk() }
          jsonPath("$.content.length()") { value(10) }
        }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = ["1"])
    fun `given user with access to a single library when getting single collection with items from 2 libraries then it is filtered`() {
      makeCollections()

      mockMvc
        .get("/api/v1/collections/${colLibBoth.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.seriesIds.length()") { value(5) }
          jsonPath("$.filtered") { value(true) }
        }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = ["1"])
    fun `given user with access to a single library when getting single collection from another library then return not found`() {
      makeCollections()

      mockMvc
        .get("/api/v1/collections/${colLib2.id}")
        .andExpect {
          status { isNotFound() }
        }
    }
  }

  @Nested
  inner class ContentRestriction {
    @Test
    @WithMockCustomUser(allowAgeUnder = 10)
    fun `given user only allowed content with specific age rating when getting collections then only get collections that satisfies this criteria`() {
      val series10 =
        makeSeries(name = "series_10", libraryId = library1.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library1.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(ageRating = 10))
          }
        }

      val series =
        makeSeries(name = "series_no", libraryId = library1.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library1.id))
            seriesLifecycle.addBooks(created, books)
          }
        }

      val colAllowed =
        collectionLifecycle.addCollection(
          SeriesCollection(
            name = "Allowed",
            seriesIds = listOf(series10.id),
          ),
        )

      val colFiltered =
        collectionLifecycle.addCollection(
          SeriesCollection(
            name = "Filtered",
            seriesIds = listOf(series10.id, series.id),
          ),
        )

      val colDenied =
        collectionLifecycle.addCollection(
          SeriesCollection(
            name = "Denied",
            seriesIds = listOf(series.id),
          ),
        )

      mockMvc
        .get("/api/v1/collections")
        .andExpect {
          status { isOk() }
          jsonPath("$.totalElements") { value(2) }
          jsonPath("$.content[?(@.name == '${colAllowed.name}')].filtered") { value(false) }
          jsonPath("$.content[?(@.name == '${colFiltered.name}')].filtered") { value(true) }
        }

      mockMvc
        .get("/api/v1/collections/${colAllowed.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.seriesIds.length()") { value(1) }
          jsonPath("$.filtered") { value(false) }
        }

      mockMvc
        .get("/api/v1/collections/${colFiltered.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.seriesIds.length()") { value(1) }
          jsonPath("$.filtered") { value(true) }
        }

      mockMvc
        .get("/api/v1/collections/${colDenied.id}")
        .andExpect {
          status { isNotFound() }
        }

      mockMvc
        .get("/api/v1/series/${series10.id}/collections")
        .andExpect {
          status { isOk() }
          jsonPath("$.length()") { value(2) }
          jsonPath("$[?(@.name == '${colAllowed.name}')].filtered") { value(false) }
          jsonPath("$[?(@.name == '${colFiltered.name}')].filtered") { value(true) }
        }
    }

    @Test
    @WithMockCustomUser(excludeAgeOver = 16)
    fun `given user disallowed content with specific age rating when getting collections then only gets collections that satisfies this criteria`() {
      val series10 =
        makeSeries(name = "series_10", libraryId = library1.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library1.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(ageRating = 10))
          }
        }

      val series18 =
        makeSeries(name = "series_18", libraryId = library1.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library1.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(ageRating = 18))
          }
        }

      val series16 =
        makeSeries(name = "series_16", libraryId = library1.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library1.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(ageRating = 16))
          }
        }

      val series =
        makeSeries(name = "series_no", libraryId = library1.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library1.id))
            seriesLifecycle.addBooks(created, books)
          }
        }

      val colAllowed =
        collectionLifecycle.addCollection(
          SeriesCollection(
            name = "Allowed",
            seriesIds = listOf(series10.id, series.id),
          ),
        )

      val colFiltered =
        collectionLifecycle.addCollection(
          SeriesCollection(
            name = "Filtered",
            seriesIds = listOf(series10.id, series16.id, series18.id, series.id),
          ),
        )

      val colDenied =
        collectionLifecycle.addCollection(
          SeriesCollection(
            name = "Denied",
            seriesIds = listOf(series16.id, series18.id),
          ),
        )

      mockMvc
        .get("/api/v1/collections")
        .andExpect {
          status { isOk() }
          jsonPath("$.totalElements") { value(2) }
          jsonPath("$.content[?(@.name == '${colAllowed.name}')].filtered") { value(false) }
          jsonPath("$.content[?(@.name == '${colFiltered.name}')].filtered") { value(true) }
        }

      mockMvc
        .get("/api/v1/collections/${colAllowed.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.seriesIds.length()") { value(2) }
          jsonPath("$.filtered") { value(false) }
        }

      mockMvc
        .get("/api/v1/collections/${colFiltered.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.seriesIds.length()") { value(2) }
          jsonPath("$.filtered") { value(true) }
        }

      mockMvc
        .get("/api/v1/collections/${colDenied.id}")
        .andExpect {
          status { isNotFound() }
        }

      mockMvc
        .get("/api/v1/series/${series10.id}/collections")
        .andExpect {
          status { isOk() }
          jsonPath("$.length()") { value(2) }
          jsonPath("$[?(@.name == '${colAllowed.name}')].filtered") { value(false) }
          jsonPath("$[?(@.name == '${colFiltered.name}')].filtered") { value(true) }
        }
    }

    @Test
    @WithMockCustomUser(allowLabels = ["kids", "cute"])
    fun `given user allowed only content with specific labels when getting series then only gets series that satisfies this criteria`() {
      val seriesKids =
        makeSeries(name = "series_kids", libraryId = library1.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library1.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(sharingLabels = setOf("kids")))
          }
        }

      val seriesCute =
        makeSeries(name = "series_cute", libraryId = library1.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library1.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(sharingLabels = setOf("cute", "other")))
          }
        }

      val seriesAdult =
        makeSeries(name = "series_adult", libraryId = library1.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library1.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(sharingLabels = setOf("adult")))
          }
        }

      val series =
        makeSeries(name = "series_no", libraryId = library1.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library1.id))
            seriesLifecycle.addBooks(created, books)
          }
        }

      val colAllowed =
        collectionLifecycle.addCollection(
          SeriesCollection(
            name = "Allowed",
            seriesIds = listOf(seriesKids.id, seriesCute.id),
          ),
        )

      val colFiltered =
        collectionLifecycle.addCollection(
          SeriesCollection(
            name = "Filtered",
            seriesIds = listOf(series.id, seriesKids.id, seriesCute.id, seriesAdult.id),
          ),
        )

      val colDenied =
        collectionLifecycle.addCollection(
          SeriesCollection(
            name = "Denied",
            seriesIds = listOf(seriesAdult.id, series.id),
          ),
        )

      mockMvc
        .get("/api/v1/collections")
        .andExpect {
          status { isOk() }
          jsonPath("$.totalElements") { value(2) }
          jsonPath("$.content[?(@.name == '${colAllowed.name}')].filtered") { value(false) }
          jsonPath("$.content[?(@.name == '${colFiltered.name}')].filtered") { value(true) }
        }

      mockMvc
        .get("/api/v1/collections/${colAllowed.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.seriesIds.length()") { value(2) }
          jsonPath("$.filtered") { value(false) }
        }

      mockMvc
        .get("/api/v1/collections/${colFiltered.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.seriesIds.length()") { value(2) }
          jsonPath("$.filtered") { value(true) }
        }

      mockMvc
        .get("/api/v1/collections/${colDenied.id}")
        .andExpect {
          status { isNotFound() }
        }

      mockMvc
        .get("/api/v1/series/${seriesKids.id}/collections")
        .andExpect {
          status { isOk() }
          jsonPath("$.length()") { value(2) }
          jsonPath("$[?(@.name == '${colAllowed.name}')].filtered") { value(false) }
          jsonPath("$[?(@.name == '${colFiltered.name}')].filtered") { value(true) }
        }
    }

    @Test
    @WithMockCustomUser(excludeLabels = ["kids", "cute"])
    fun `given user disallowed content with specific labels when getting series then only gets series that satisfies this criteria`() {
      val seriesKids =
        makeSeries(name = "series_kids", libraryId = library1.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library1.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(sharingLabels = setOf("kids")))
          }
        }

      val seriesCute =
        makeSeries(name = "series_cute", libraryId = library1.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library1.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(sharingLabels = setOf("cute", "other")))
          }
        }

      val seriesAdult =
        makeSeries(name = "series_adult", libraryId = library1.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library1.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(sharingLabels = setOf("adult")))
          }
        }

      val series =
        makeSeries(name = "series_no", libraryId = library1.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library1.id))
            seriesLifecycle.addBooks(created, books)
          }
        }

      val colAllowed =
        collectionLifecycle.addCollection(
          SeriesCollection(
            name = "Allowed",
            seriesIds = listOf(seriesAdult.id, series.id),
          ),
        )

      val colFiltered =
        collectionLifecycle.addCollection(
          SeriesCollection(
            name = "Filtered",
            seriesIds = listOf(seriesAdult.id, seriesCute.id, seriesKids.id, series.id),
          ),
        )

      val colDenied =
        collectionLifecycle.addCollection(
          SeriesCollection(
            name = "Denied",
            seriesIds = listOf(seriesKids.id, seriesCute.id),
          ),
        )

      mockMvc
        .get("/api/v1/collections")
        .andExpect {
          status { isOk() }
          jsonPath("$.totalElements") { value(2) }
          jsonPath("$.content[?(@.name == '${colAllowed.name}')].filtered") { value(false) }
          jsonPath("$.content[?(@.name == '${colFiltered.name}')].filtered") { value(true) }
        }

      mockMvc
        .get("/api/v1/collections/${colAllowed.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.seriesIds.length()") { value(2) }
          jsonPath("$.filtered") { value(false) }
        }

      mockMvc
        .get("/api/v1/collections/${colFiltered.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.seriesIds.length()") { value(2) }
          jsonPath("$.filtered") { value(true) }
        }

      mockMvc
        .get("/api/v1/collections/${colDenied.id}")
        .andExpect {
          status { isNotFound() }
        }

      mockMvc
        .get("/api/v1/series/${series.id}/collections")
        .andExpect {
          status { isOk() }
          jsonPath("$.length()") { value(2) }
          jsonPath("$[?(@.name == '${colAllowed.name}')].filtered") { value(false) }
          jsonPath("$[?(@.name == '${colFiltered.name}')].filtered") { value(true) }
        }
    }

    @Test
    @WithMockCustomUser(allowAgeUnder = 10, allowLabels = ["kids"], excludeLabels = ["adult", "teen"])
    fun `given user allowed and disallowed content when getting series then only gets series that satisfies this criteria`() {
      val seriesKids =
        makeSeries(name = "series_kids", libraryId = library1.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library1.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(sharingLabels = setOf("kids")))
          }
        }

      val seriesCute =
        makeSeries(name = "series_cute", libraryId = library1.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library1.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(ageRating = 5, sharingLabels = setOf("cute", "other")))
          }
        }

      val seriesAdult =
        makeSeries(name = "series_adult", libraryId = library1.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library1.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(sharingLabels = setOf("adult")))
          }
        }

      val series =
        makeSeries(name = "series_no", libraryId = library1.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library1.id))
            seriesLifecycle.addBooks(created, books)
          }
        }

      val colAllowed =
        collectionLifecycle.addCollection(
          SeriesCollection(
            name = "Allowed",
            seriesIds = listOf(seriesKids.id, seriesCute.id),
          ),
        )

      val colFiltered =
        collectionLifecycle.addCollection(
          SeriesCollection(
            name = "Filtered",
            seriesIds = listOf(series.id, seriesKids.id, seriesCute.id, seriesAdult.id),
          ),
        )

      val colDenied =
        collectionLifecycle.addCollection(
          SeriesCollection(
            name = "Denied",
            seriesIds = listOf(seriesAdult.id, series.id),
          ),
        )

      mockMvc
        .get("/api/v1/collections")
        .andExpect {
          status { isOk() }
          jsonPath("$.totalElements") { value(2) }
          jsonPath("$.content[?(@.name == '${colAllowed.name}')].filtered") { value(false) }
          jsonPath("$.content[?(@.name == '${colFiltered.name}')].filtered") { value(true) }
        }

      mockMvc
        .get("/api/v1/collections/${colAllowed.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.seriesIds.length()") { value(2) }
          jsonPath("$.filtered") { value(false) }
        }

      mockMvc
        .get("/api/v1/collections/${colFiltered.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.seriesIds.length()") { value(2) }
          jsonPath("$.filtered") { value(true) }
        }

      mockMvc
        .get("/api/v1/collections/${colDenied.id}")
        .andExpect {
          status { isNotFound() }
        }

      mockMvc
        .get("/api/v1/series/${seriesKids.id}/collections")
        .andExpect {
          status { isOk() }
          jsonPath("$.length()") { value(2) }
          jsonPath("$[?(@.name == '${colAllowed.name}')].filtered") { value(false) }
          jsonPath("$[?(@.name == '${colFiltered.name}')].filtered") { value(true) }
        }
    }

    @Test
    @WithMockCustomUser(excludeAgeOver = 16, allowLabels = ["teen"])
    fun `given user allowed and disallowed content when getting series then only gets series that satisfies this criteria (2)`() {
      val seriesTeen16 =
        makeSeries(name = "series_teen_16", libraryId = library1.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library1.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(sharingLabels = setOf("teen"), ageRating = 16))
          }
        }

      val seriesTeen =
        makeSeries(name = "series_teen", libraryId = library1.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library1.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(sharingLabels = setOf("teen")))
          }
        }

      val colAllowed =
        collectionLifecycle.addCollection(
          SeriesCollection(
            name = "Allowed",
            seriesIds = listOf(seriesTeen.id),
          ),
        )

      val colFiltered =
        collectionLifecycle.addCollection(
          SeriesCollection(
            name = "Filtered",
            seriesIds = listOf(seriesTeen16.id, seriesTeen.id),
          ),
        )

      val colDenied =
        collectionLifecycle.addCollection(
          SeriesCollection(
            name = "Denied",
            seriesIds = listOf(seriesTeen16.id),
          ),
        )

      mockMvc
        .get("/api/v1/collections")
        .andExpect {
          status { isOk() }
          jsonPath("$.totalElements") { value(2) }
          jsonPath("$.content[?(@.name == '${colAllowed.name}')].filtered") { value(false) }
          jsonPath("$.content[?(@.name == '${colFiltered.name}')].filtered") { value(true) }
        }

      mockMvc
        .get("/api/v1/collections/${colAllowed.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.seriesIds.length()") { value(1) }
          jsonPath("$.filtered") { value(false) }
        }

      mockMvc
        .get("/api/v1/collections/${colFiltered.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.seriesIds.length()") { value(1) }
          jsonPath("$.filtered") { value(true) }
        }

      mockMvc
        .get("/api/v1/collections/${colDenied.id}")
        .andExpect {
          status { isNotFound() }
        }

      mockMvc
        .get("/api/v1/series/${seriesTeen.id}/collections")
        .andExpect {
          status { isOk() }
          jsonPath("$.length()") { value(2) }
          jsonPath("$[?(@.name == '${colAllowed.name}')].filtered") { value(false) }
          jsonPath("$[?(@.name == '${colFiltered.name}')].filtered") { value(true) }
        }
    }
  }

  @Nested
  inner class Creation {
    @Test
    @WithMockCustomUser
    fun `given non-admin user when creating collection then return forbidden`() {
      // language=JSON
      val jsonString =
        """
        {"name":"collection","ordered":false,"seriesIds":["3"]}
        """.trimIndent()

      mockMvc
        .post("/api/v1/collections") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isForbidden() }
        }
    }

    @Test
    @WithMockCustomUser(roles = ["ADMIN"])
    fun `given admin user when creating collection then return ok`() {
      // language=JSON
      val jsonString =
        """
        {"name":"collection","ordered":false,"seriesIds":["${seriesLibrary1.first().id}"]}
        """.trimIndent()

      mockMvc
        .post("/api/v1/collections") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isOk() }
          jsonPath("$.seriesIds.length()") { value(1) }
          jsonPath("$.name") { value("collection") }
          jsonPath("$.ordered") { value(false) }
        }
    }

    @Test
    @WithMockCustomUser(roles = ["ADMIN"])
    fun `given existing collections when creating collection with existing name then return bad request`() {
      makeCollections()

      // language=JSON
      val jsonString =
        """
        {"name":"Lib1","ordered":false,"seriesIds":["${seriesLibrary1.first().id}"]}
        """.trimIndent()

      mockMvc
        .post("/api/v1/collections") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isBadRequest() }
        }
    }

    @Test
    @WithMockCustomUser(roles = ["ADMIN"])
    fun `given collection with duplicate seriesIds when creating collection then return bad request`() {
      // language=JSON
      val jsonString =
        """
        {"name":"Lib1","ordered":false,"seriesIds":["${seriesLibrary1.first().id}","${seriesLibrary1.first().id}"]}
        """.trimIndent()

      mockMvc
        .post("/api/v1/collections") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isBadRequest() }
        }
    }
  }

  @Nested
  inner class Update {
    @Test
    @WithMockCustomUser
    fun `given non-admin user when updating collection then return forbidden`() {
      // language=JSON
      val jsonString =
        """
        {"name":"collection","ordered":false,"seriesIds":["3"]}
        """.trimIndent()

      mockMvc
        .patch("/api/v1/collections/5") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isForbidden() }
        }
    }

    @Test
    @WithMockCustomUser(roles = ["ADMIN"])
    fun `given admin user when updating collection then return no content`() {
      makeCollections()

      // language=JSON
      val jsonString =
        """
        {"name":"updated","ordered":true,"seriesIds":["${seriesLibrary1.first().id}"]}
        """.trimIndent()

      mockMvc
        .patch("/api/v1/collections/${colLib1.id}") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isNoContent() }
        }

      mockMvc
        .get("/api/v1/collections/${colLib1.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.name") { value("updated") }
          jsonPath("$.ordered") { value(true) }
          jsonPath("$.seriesIds.length()") { value(1) }
          jsonPath("$.filtered") { value(false) }
        }
    }

    @Test
    @WithMockCustomUser(roles = ["ADMIN"])
    fun `given existing collections when updating collection with existing name then return bad request`() {
      makeCollections()

      // language=JSON
      val jsonString = """{"name":"Lib2"}"""

      mockMvc
        .patch("/api/v1/collections/${colLib1.id}") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isBadRequest() }
        }
    }

    @Test
    @WithMockCustomUser(roles = ["ADMIN"])
    fun `given existing collection when updating collection with duplicate seriesIds then return bad request`() {
      makeCollections()

      // language=JSON
      val jsonString = """{"seriesIds":["${seriesLibrary1.first().id}","${seriesLibrary1.first().id}"]}"""

      mockMvc
        .patch("/api/v1/collections/${colLib1.id}") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isBadRequest() }
        }
    }

    @Test
    @WithMockCustomUser(roles = ["ADMIN"])
    fun `given admin user when updating collection then only updated fields are modified`() {
      makeCollections()

      mockMvc.patch("/api/v1/collections/${colLib1.id}") {
        contentType = MediaType.APPLICATION_JSON
        content = """{"ordered":true}"""
      }

      mockMvc
        .get("/api/v1/collections/${colLib1.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.name") { value("Lib1") }
          jsonPath("$.ordered") { value(true) }
          jsonPath("$.seriesIds.length()") { value(5) }
        }

      mockMvc.patch("/api/v1/collections/${colLib2.id}") {
        contentType = MediaType.APPLICATION_JSON
        content = """{"name":"newName"}"""
      }

      mockMvc
        .get("/api/v1/collections/${colLib2.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.name") { value("newName") }
          jsonPath("$.ordered") { value(false) }
          jsonPath("$.seriesIds.length()") { value(5) }
        }

      mockMvc.patch("/api/v1/collections/${colLibBoth.id}") {
        contentType = MediaType.APPLICATION_JSON
        content = """{"seriesIds":["${seriesLibrary1.first().id}"]}"""
      }

      mockMvc
        .get("/api/v1/collections/${colLibBoth.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.name") { value("Lib1+2") }
          jsonPath("$.ordered") { value(true) }
          jsonPath("$.seriesIds.length()") { value(1) }
        }
    }
  }

  @Nested
  inner class Delete {
    @Test
    @WithMockCustomUser
    fun `given non-admin user when deleting collection then return forbidden`() {
      mockMvc
        .delete("/api/v1/collections/5")
        .andExpect {
          status { isForbidden() }
        }
    }

    @Test
    @WithMockCustomUser(roles = ["ADMIN"])
    fun `given admin user when deleting collection then return no content`() {
      makeCollections()

      mockMvc
        .delete("/api/v1/collections/${colLib1.id}")
        .andExpect {
          status { isNoContent() }
        }

      mockMvc
        .get("/api/v1/collections/${colLib1.id}")
        .andExpect {
          status { isNotFound() }
        }
    }
  }
}
