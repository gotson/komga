package org.gotson.komga.interfaces.api.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.BookPage
import org.gotson.komga.domain.model.Dimension
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.MarkSelectedPreference
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.ThumbnailBook
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.service.BookLifecycle
import org.gotson.komga.domain.service.FileSystemScanner
import org.gotson.komga.domain.service.KomgaUserLifecycle
import org.gotson.komga.domain.service.LibraryContentLifecycle
import org.gotson.komga.domain.service.LibraryLifecycle
import org.gotson.komga.domain.service.SeriesLifecycle
import org.gotson.komga.toScanResult
import org.hamcrest.Matchers
import org.hamcrest.core.IsNull
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MockMvcResultMatchersDsl
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import kotlin.random.Random

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class SeriesControllerTest(
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val seriesLifecycle: SeriesLifecycle,
  @Autowired private val seriesMetadataRepository: SeriesMetadataRepository,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val libraryLifecycle: LibraryLifecycle,
  @Autowired private val libraryContentLifecycle: LibraryContentLifecycle,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val bookLifecycle: BookLifecycle,
  @Autowired private val mediaRepository: MediaRepository,
  @Autowired private val bookMetadataRepository: BookMetadataRepository,
  @Autowired private val userRepository: KomgaUserRepository,
  @Autowired private val userLifecycle: KomgaUserLifecycle,
  @Autowired private val mockMvc: MockMvc,
) {
  @MockkBean
  private lateinit var mockScanner: FileSystemScanner

  private val library = makeLibrary(id = "1")

  @BeforeAll
  fun `setup library`() {
    libraryRepository.insert(library)
    userRepository.insert(KomgaUser("user@example.org", "", id = "1"))
  }

  @AfterAll
  fun teardown() {
    userRepository.findAll().forEach {
      userLifecycle.deleteUser(it)
    }
    libraryRepository.findAll().forEach {
      libraryLifecycle.deleteLibrary(it)
    }
  }

  @AfterEach
  fun `clear repository`() {
    seriesLifecycle.deleteMany(seriesRepository.findAll())
  }

  @Nested
  inner class Search {
    @Test
    @WithMockCustomUser
    fun `given series when searching by regex then series are found`() {
      val alphaC = seriesLifecycle.createSeries(makeSeries("TheAlpha", libraryId = library.id))
      seriesMetadataRepository.findById(alphaC.id).let {
        seriesMetadataRepository.update(it.copy(titleSort = "Alpha, The"))
      }
      seriesLifecycle.createSeries(makeSeries("TheBeta", libraryId = library.id))

      mockMvc
        .get("/api/v1/series") {
          param("search_regex", "a$,title_sort")
        }.andExpect {
          status { isOk() }
          jsonPath("$.content.length()") { value(1) }
          jsonPath("$.content[0].metadata.title") { value("TheBeta") }
        }

      mockMvc
        .get("/api/v1/series") {
          param("search_regex", "^the,title")
        }.andExpect {
          status { isOk() }
          jsonPath("$.content.length()") { value(2) }
          jsonPath("$.content[0].metadata.title") { value("TheAlpha") }
          jsonPath("$.content[1].metadata.title") { value("TheBeta") }
        }
    }
  }

  @Nested
  inner class SeriesSort {
    @Test
    @WithMockCustomUser
    fun `given series with titleSort when requesting via api then series are sorted by titleSort`() {
      val alphaC = seriesLifecycle.createSeries(makeSeries("TheAlpha", libraryId = library.id))
      seriesMetadataRepository.findById(alphaC.id).let {
        seriesMetadataRepository.update(it.copy(titleSort = "Alpha, The"))
      }
      seriesLifecycle.createSeries(makeSeries("Beta", libraryId = library.id))

      mockMvc
        .get("/api/v1/series")
        .andExpect {
          status { isOk() }
          jsonPath("$.content[0].metadata.title") { value("TheAlpha") }
          jsonPath("$.content[1].metadata.title") { value("Beta") }
        }
    }

    @Test
    @WithMockCustomUser
    fun `given series when requesting via api then series are sorted insensitive of case`() {
      listOf("a", "b", "B", "C")
        .map { name -> makeSeries(name, libraryId = library.id) }
        .forEach {
          seriesLifecycle.createSeries(it)
        }

      mockMvc
        .get("/api/v1/series") {
          param("sort", "metadata.titleSort,asc")
        }.andExpect {
          status { isOk() }
          jsonPath("$.content[0].metadata.title") { value("a") }
          jsonPath("$.content[1].metadata.title") { value(Matchers.equalToIgnoringCase("b")) }
          jsonPath("$.content[2].metadata.title") { value(Matchers.equalToIgnoringCase("b")) }
          jsonPath("$.content[3].metadata.title") { value("C") }
        }
    }
  }

  @Nested
  inner class BookOrdering {
    @Test
    @WithMockCustomUser
    fun `given books with unordered index when requesting via api then books are ordered`() {
      val createdSeries =
        makeSeries(name = "series", libraryId = library.id).let { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id), makeBook("3", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
        }

      val addedBook = makeBook("2", libraryId = library.id)
      seriesLifecycle.addBooks(createdSeries, listOf(addedBook))
      seriesLifecycle.sortBooks(createdSeries)

      mockMvc
        .get("/api/v1/series/${createdSeries.id}/books")
        .andExpect {
          status { isOk() }
          jsonPath("$.content[0].name") { value("1") }
          jsonPath("$.content[1].name") { value("2") }
          jsonPath("$.content[2].name") { value("3") }
        }
    }

    @Test
    @WithMockCustomUser
    fun `given many books with unordered index when requesting via api then books are ordered and paged`() {
      val createdSeries =
        makeSeries(name = "series", libraryId = library.id).let { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = (1..100 step 2).map { makeBook("$it", libraryId = library.id) }
            seriesLifecycle.addBooks(created, books)
          }
        }

      val addedBook = makeBook("2", libraryId = library.id)
      seriesLifecycle.addBooks(createdSeries, listOf(addedBook))
      seriesLifecycle.sortBooks(createdSeries)

      mockMvc
        .get("/api/v1/series/${createdSeries.id}/books")
        .andExpect {
          status { isOk() }
          jsonPath("$.content[0].name") { value("1") }
          jsonPath("$.content[1].name") { value("2") }
          jsonPath("$.content[2].name") { value("3") }
          jsonPath("$.content[3].name") { value("5") }
          jsonPath("$.size") { value(20) }
          jsonPath("$.first") { value(true) }
          jsonPath("$.number") { value(0) }
        }
    }
  }

  @Nested
  inner class LimitedUser {
    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = ["1"])
    fun `given user with access to a single library when getting series then only gets series from this library`() {
      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val otherLibrary = makeLibrary("other")
      libraryRepository.insert(otherLibrary)
      makeSeries(name = "otherSeries", libraryId = otherLibrary.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("2", libraryId = otherLibrary.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      mockMvc
        .get("/api/v1/series")
        .andExpect {
          status { isOk() }
          jsonPath("$.content.length()") { value(1) }
          jsonPath("$.content[0].name") { value("series") }
        }
    }
  }

  @Nested
  inner class ContentRestrictedUser {
    @Test
    @WithMockCustomUser(allowAgeUnder = 10)
    fun `given user only allowed content with specific age rating when getting series then only gets series that satisfies this criteria`() {
      val series10 =
        makeSeries(name = "series_10", libraryId = library.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(ageRating = 10))
          }
        }

      val series5 =
        makeSeries(name = "series_5", libraryId = library.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(ageRating = 5))
          }
        }

      val series15 =
        makeSeries(name = "series_15", libraryId = library.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(ageRating = 15))
          }
        }

      val series =
        makeSeries(name = "series_no", libraryId = library.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
        }

      mockMvc.get("/api/v1/series/${series5.id}").andExpect { status { isOk() } }
      mockMvc.get("/api/v1/series/${series10.id}").andExpect { status { isOk() } }
      mockMvc.get("/api/v1/series/${series15.id}").andExpect { status { isForbidden() } }
      mockMvc.get("/api/v1/series/${series.id}").andExpect { status { isForbidden() } }

      mockMvc
        .get("/api/v1/series?sort=metadata.titleSort")
        .andExpect {
          status { isOk() }
          jsonPath("$.content.length()") { value(2) }
          jsonPath("$.content[0].name") { value("series_10") }
          jsonPath("$.content[1].name") { value("series_5") }
        }
    }

    @Test
    @WithMockCustomUser(excludeAgeOver = 16)
    fun `given user disallowed content with specific age rating when getting series then only gets series that satisfies this criteria`() {
      val series10 =
        makeSeries(name = "series_10", libraryId = library.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(ageRating = 10))
          }
        }

      val series18 =
        makeSeries(name = "series_18", libraryId = library.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(ageRating = 18))
          }
        }

      val series16 =
        makeSeries(name = "series_16", libraryId = library.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(ageRating = 16))
          }
        }

      val series =
        makeSeries(name = "series_no", libraryId = library.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
        }

      mockMvc.get("/api/v1/series/${series.id}").andExpect { status { isOk() } }
      mockMvc.get("/api/v1/series/${series10.id}").andExpect { status { isOk() } }
      mockMvc.get("/api/v1/series/${series16.id}").andExpect { status { isForbidden() } }
      mockMvc.get("/api/v1/series/${series18.id}").andExpect { status { isForbidden() } }

      mockMvc
        .get("/api/v1/series?sort=metadata.titleSort")
        .andExpect {
          status { isOk() }
          jsonPath("$.content.length()") { value(2) }
          jsonPath("$.content[0].name") { value("series_10") }
          jsonPath("$.content[1].name") { value("series_no") }
        }
    }

    @Test
    @WithMockCustomUser(allowLabels = ["kids", "cute"])
    fun `given user allowed only content with specific labels when getting series then only gets series that satisfies this criteria`() {
      val seriesKids =
        makeSeries(name = "series_kids", libraryId = library.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(sharingLabels = setOf("kids")))
          }
        }

      val seriesCute =
        makeSeries(name = "series_cute", libraryId = library.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(sharingLabels = setOf("cute", "other")))
          }
        }

      val seriesAdult =
        makeSeries(name = "series_adult", libraryId = library.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(sharingLabels = setOf("adult")))
          }
        }

      val series =
        makeSeries(name = "series_no", libraryId = library.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
        }

      mockMvc.get("/api/v1/series/${seriesKids.id}").andExpect { status { isOk() } }
      mockMvc.get("/api/v1/series/${seriesCute.id}").andExpect { status { isOk() } }
      mockMvc.get("/api/v1/series/${seriesAdult.id}").andExpect { status { isForbidden() } }
      mockMvc.get("/api/v1/series/${series.id}").andExpect { status { isForbidden() } }

      mockMvc
        .get("/api/v1/series?sort=metadata.titleSort")
        .andExpect {
          status { isOk() }
          jsonPath("$.content.length()") { value(2) }
          jsonPath("$.content[0].name") { value(seriesCute.name) }
          jsonPath("$.content[1].name") { value(seriesKids.name) }
        }
    }

    @Test
    @WithMockCustomUser(excludeLabels = ["kids", "cute"])
    fun `given user disallowed content with specific labels when getting series then only gets series that satisfies this criteria`() {
      val seriesKids =
        makeSeries(name = "series_kids", libraryId = library.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(sharingLabels = setOf("kids")))
          }
        }

      val seriesCute =
        makeSeries(name = "series_cute", libraryId = library.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(sharingLabels = setOf("cute", "other")))
          }
        }

      val seriesAdult =
        makeSeries(name = "series_adult", libraryId = library.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(sharingLabels = setOf("adult")))
          }
        }

      val series =
        makeSeries(name = "series_no", libraryId = library.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
        }

      mockMvc.get("/api/v1/series/${seriesKids.id}").andExpect { status { isForbidden() } }
      mockMvc.get("/api/v1/series/${seriesCute.id}").andExpect { status { isForbidden() } }
      mockMvc.get("/api/v1/series/${seriesAdult.id}").andExpect { status { isOk() } }
      mockMvc.get("/api/v1/series/${series.id}").andExpect { status { isOk() } }

      mockMvc
        .get("/api/v1/series?sort=metadata.titleSort")
        .andExpect {
          status { isOk() }
          jsonPath("$.content.length()") { value(2) }
          jsonPath("$.content[0].name") { value(seriesAdult.name) }
          jsonPath("$.content[1].name") { value(series.name) }
        }
    }

    @Test
    @WithMockCustomUser(allowAgeUnder = 10, allowLabels = ["kids"], excludeLabels = ["adult", "teen"])
    fun `given user allowed and disallowed content when getting series then only gets series that satisfies this criteria`() {
      val seriesKids =
        makeSeries(name = "series_kids", libraryId = library.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(sharingLabels = setOf("kids")))
          }
        }

      val seriesCute =
        makeSeries(name = "series_cute", libraryId = library.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(ageRating = 5, sharingLabels = setOf("cute", "other")))
          }
        }

      val seriesAdult =
        makeSeries(name = "series_adult", libraryId = library.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(sharingLabels = setOf("adult")))
          }
        }

      val series =
        makeSeries(name = "series_no", libraryId = library.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
        }

      mockMvc.get("/api/v1/series/${seriesKids.id}").andExpect { status { isOk() } }
      mockMvc.get("/api/v1/series/${seriesCute.id}").andExpect { status { isOk() } }
      mockMvc.get("/api/v1/series/${seriesAdult.id}").andExpect { status { isForbidden() } }
      mockMvc.get("/api/v1/series/${series.id}").andExpect { status { isForbidden() } }

      mockMvc
        .get("/api/v1/series?sort=metadata.titleSort")
        .andExpect {
          status { isOk() }
          jsonPath("$.content.length()") { value(2) }
          jsonPath("$.content[0].name") { value(seriesCute.name) }
          jsonPath("$.content[1].name") { value(seriesKids.name) }
        }
    }

    @Test
    @WithMockCustomUser(excludeAgeOver = 16, allowLabels = ["teen"])
    fun `given user allowed and disallowed content when getting series then only gets series that satisfies this criteria (2)`() {
      val seriesTeen16 =
        makeSeries(name = "series_teen_16", libraryId = library.id).also { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
          seriesMetadataRepository.findById(series.id).let {
            seriesMetadataRepository.update(it.copy(sharingLabels = setOf("teen"), ageRating = 16))
          }
        }

      mockMvc.get("/api/v1/series/${seriesTeen16.id}").andExpect { status { isForbidden() } }

      mockMvc
        .get("/api/v1/series")
        .andExpect {
          status { isOk() }
          jsonPath("$.content.length()") { value(0) }
        }
    }
  }

  @Nested
  inner class UserWithoutLibraryAccess {
    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = [])
    fun `given user with no access to any library when getting specific series then returns forbidden`() {
      val createdSeries =
        makeSeries(name = "series", libraryId = library.id).let { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
        }

      mockMvc
        .get("/api/v1/series/${createdSeries.id}")
        .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = [])
    fun `given user with no access to any library when getting specific series thumbnail then returns forbidden`() {
      val createdSeries =
        makeSeries(name = "series", libraryId = library.id).let { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
        }

      mockMvc
        .get("/api/v1/series/${createdSeries.id}/thumbnail")
        .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = [])
    fun `given user with no access to any library when getting specific series books then returns forbidden`() {
      val createdSeries =
        makeSeries(name = "series", libraryId = library.id).let { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
        }

      mockMvc
        .get("/api/v1/series/${createdSeries.id}/books")
        .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = [])
    fun `given user with no access to any library when getting specific series file then returns forbidden`() {
      val createdSeries =
        makeSeries(name = "series", libraryId = library.id).let { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
        }

      mockMvc
        .get("/api/v1/series/${createdSeries.id}/file")
        .andExpect { status { isForbidden() } }
    }
  }

  @Nested
  inner class RestrictedUserByRole {
    @Test
    @WithMockCustomUser(roles = [])
    fun `given user without file download role when getting specific series file then returns forbidden`() {
      val createdSeries =
        makeSeries(name = "series", libraryId = library.id).let { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
        }

      mockMvc
        .get("/api/v1/series/${createdSeries.id}/file")
        .andExpect { status { isForbidden() } }
    }
  }

  @Nested
  inner class MediaNotReady {
    @Test
    @WithMockCustomUser
    fun `given book without thumbnail when getting series thumbnail then returns not found`() {
      val createdSeries =
        makeSeries(name = "series", libraryId = library.id).let { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
        }

      mockMvc
        .get("/api/v1/series/${createdSeries.id}/thumbnail")
        .andExpect { status { isNotFound() } }
    }
  }

  @Nested
  inner class DtoUrlSanitization {
    @Test
    @WithMockCustomUser
    fun `given regular user when getting series then url is hidden`() {
      val createdSeries =
        makeSeries(name = "series", libraryId = library.id).let { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
        }

      val validation: MockMvcResultMatchersDsl.() -> Unit = {
        status { isOk() }
        jsonPath("$.content[0].url") { value("") }
      }

      mockMvc
        .get("/api/v1/series")
        .andExpect(validation)

      mockMvc
        .get("/api/v1/series/latest")
        .andExpect(validation)

      mockMvc
        .get("/api/v1/series/new")
        .andExpect(validation)

      mockMvc
        .get("/api/v1/series/${createdSeries.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.url") { value("") }
        }
    }

    @Test
    @WithMockCustomUser(roles = ["ADMIN"])
    fun `given admin user when getting series then url is available`() {
      val createdSeries =
        makeSeries(name = "series", libraryId = library.id).let { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
        }

      val validation: MockMvcResultMatchersDsl.() -> Unit = {
        status { isOk() }
        jsonPath("$.content[0].url") { value(Matchers.containsString("series")) }
      }

      mockMvc
        .get("/api/v1/series")
        .andExpect(validation)

      mockMvc
        .get("/api/v1/series/latest")
        .andExpect(validation)

      mockMvc
        .get("/api/v1/series/new")
        .andExpect(validation)

      mockMvc
        .get("/api/v1/series/${createdSeries.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.url") { value(Matchers.containsString("series")) }
        }
    }
  }

  @Nested
  inner class MetadataUpdate {
    @Test
    @WithMockCustomUser
    fun `given non-admin user when updating metadata then raise forbidden`() {
      mockMvc
        .patch("/api/v1/series/1/metadata") {
          contentType = MediaType.APPLICATION_JSON
          content = "{}"
        }.andExpect {
          status { isForbidden() }
        }
    }

    @ParameterizedTest
    @ValueSource(
      strings = [
        """{"title":""}""",
        """{"titleSort":""}""",
        """{"ageRating":-1}""",
        """{"totalBookCount":0}""",
        """{"language":"japanese"}""",
      ],
    )
    @WithMockCustomUser(roles = ["ADMIN"])
    fun `given invalid json when updating metadata then raise validation error`(jsonString: String) {
      mockMvc
        .patch("/api/v1/series/1/metadata") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isBadRequest() }
        }
    }

    @Test
    @WithMockCustomUser(roles = ["ADMIN"])
    fun `given valid json when updating metadata then fields are updated`() {
      val createdSeries =
        makeSeries(name = "series", libraryId = library.id).let { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
        }

      // language=JSON
      val jsonString =
        """
        {
          "title":"newTitle",
          "titleLock":true,
          "titleSort":"newTitleSort",
          "titleSortLock":true,
          "status":"HIATUS",
          "statusLock":true,
          "summary":"newSummary",
          "summaryLock":true,
          "readingDirection":"LEFT_TO_RIGHT",
          "readingDirectionLock":true,
          "ageRating":12,
          "ageRatingLock":true,
          "publisher":"newPublisher",
          "publisherLock":true,
          "language":"fra",
          "languageLock":true,
          "genres":["Action"],
          "genresLock":true,
          "tags":["tag"],
          "tagsLock":true,
          "totalBookCount":5,
          "totalBookCountLock":true
        }
        """.trimIndent()

      mockMvc
        .patch("/api/v1/series/${createdSeries.id}/metadata") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isNoContent() }
        }

      val updatedMetadata = seriesMetadataRepository.findById(createdSeries.id)
      with(updatedMetadata) {
        assertThat(title).isEqualTo("newTitle")
        assertThat(titleSort).isEqualTo("newTitleSort")
        assertThat(status).isEqualTo(SeriesMetadata.Status.HIATUS)
        assertThat(readingDirection).isEqualTo(SeriesMetadata.ReadingDirection.LEFT_TO_RIGHT)
        assertThat(publisher).isEqualTo("newPublisher")
        assertThat(summary).isEqualTo("newSummary")
        assertThat(language).isEqualTo("fr")
        assertThat(ageRating).isEqualTo(12)
        assertThat(genres).containsExactly("action")
        assertThat(tags).containsExactly("tag")
        assertThat(totalBookCount).isEqualTo(5)

        assertThat(titleLock).isEqualTo(true)
        assertThat(titleSortLock).isEqualTo(true)
        assertThat(statusLock).isEqualTo(true)
        assertThat(readingDirectionLock).isEqualTo(true)
        assertThat(publisherLock).isEqualTo(true)
        assertThat(ageRatingLock).isEqualTo(true)
        assertThat(languageLock).isEqualTo(true)
        assertThat(summaryLock).isEqualTo(true)
        assertThat(genresLock).isEqualTo(true)
        assertThat(tagsLock).isEqualTo(true)
        assertThat(totalBookCountLock).isEqualTo(true)
      }
    }

    @Test
    @WithMockCustomUser(roles = ["ADMIN"])
    fun `given json with null fields when updating metadata then fields with null are unset`() {
      val createdSeries =
        makeSeries(name = "series", libraryId = library.id).let { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
        }

      seriesMetadataRepository.findById(createdSeries.id).let { metadata ->
        val updated =
          metadata.copy(
            ageRating = 12,
            readingDirection = SeriesMetadata.ReadingDirection.LEFT_TO_RIGHT,
            genres = setOf("Action"),
            tags = setOf("tag"),
            totalBookCount = 5,
          )

        seriesMetadataRepository.update(updated)
      }

      val metadata = seriesMetadataRepository.findById(createdSeries.id)
      with(metadata) {
        assertThat(readingDirection).isEqualTo(SeriesMetadata.ReadingDirection.LEFT_TO_RIGHT)
        assertThat(ageRating).isEqualTo(12)
        assertThat(genres).hasSize(1)
      }

      // language=JSON
      val jsonString =
        """
        {
          "readingDirection":null,
          "ageRating":null,
          "genres":null,
          "tags":null,
          "totalBookCount":null
        }
        """.trimIndent()

      mockMvc
        .patch("/api/v1/series/${createdSeries.id}/metadata") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isNoContent() }
        }

      val updatedMetadata = seriesMetadataRepository.findById(createdSeries.id)
      with(updatedMetadata) {
        assertThat(readingDirection).isNull()
        assertThat(ageRating).isNull()
        assertThat(genres).isEmpty()
        assertThat(tags).isEmpty()
        assertThat(totalBookCount).isNull()
      }
    }
  }

  @Nested
  inner class HttpCache {
    @Test
    @WithMockCustomUser
    fun `given request with cache headers when getting series thumbnail then returns 304 not modified`() {
      val createdSeries =
        makeSeries(name = "series", libraryId = library.id).let { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
        }

      bookRepository.findAll().first().let { book ->
        bookLifecycle.addThumbnailForBook(
          ThumbnailBook(
            thumbnail = Random.nextBytes(1),
            bookId = book.id,
            type = ThumbnailBook.Type.GENERATED,
            fileSize = 0,
            mediaType = "",
            dimension = Dimension(0, 0),
          ),
          MarkSelectedPreference.YES,
        )
      }

      val url = "/api/v1/series/${createdSeries.id}/thumbnail"

      val response =
        mockMvc
          .get(url)
          .andReturn()
          .response

      mockMvc
        .get(url) {
          headers {
            ifNoneMatch = listOf(response.getHeader(HttpHeaders.ETAG)!!)
          }
        }.andExpect {
          status { isNotModified() }
        }
    }

    @Test
    @WithMockCustomUser
    fun `given request with cache headers and modified first book when getting series thumbnail then returns 200 ok`() {
      val createdSeries =
        makeSeries(name = "series", libraryId = library.id).let { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1", libraryId = library.id), makeBook("2", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
        }

      bookRepository.findAll().forEach { book ->
        bookLifecycle.addThumbnailForBook(
          ThumbnailBook(
            thumbnail = Random.nextBytes(1),
            bookId = book.id,
            type = ThumbnailBook.Type.GENERATED,
            fileSize = 0,
            mediaType = "",
            dimension = Dimension(0, 0),
          ),
          MarkSelectedPreference.YES,
        )
      }

      val url = "/api/v1/series/${createdSeries.id}/thumbnail"

      val response = mockMvc.get(url).andReturn().response

      bookRepository.findAll().first { it.name == "1" }.let { book ->
        bookMetadataRepository.findById(book.id).let {
          bookMetadataRepository.update(it.copy(numberSort = 3F))
        }
      }

      mockMvc
        .get(url) {
          headers {
            ifNoneMatch = listOf(response.getHeader(HttpHeaders.ETAG)!!)
          }
        }.andExpect {
          status { isOk() }
        }
    }
  }

  @Nested
  inner class ReadProgress {
    @Test
    @WithMockCustomUser(id = "1")
    fun `given user when marking series as read then progress is marked for all books`() {
      val series =
        makeSeries(name = "series", libraryId = library.id).let { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1.cbr", libraryId = library.id), makeBook("2.cbr", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
            seriesLifecycle.sortBooks(created)
          }
        }

      bookRepository.findAll().forEach { book ->
        mediaRepository.findById(book.id).let { media ->
          mediaRepository.update(
            media.copy(
              status = Media.Status.READY,
              pages = (1..10).map { BookPage("$it", "image/jpeg") },
            ),
          )
        }
      }

      mockMvc
        .post("/api/v1/series/${series.id}/read-progress")
        .andExpect {
          status { isNoContent() }
        }

      mockMvc
        .get("/api/v1/series/${series.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.booksUnreadCount") { value(0) }
          jsonPath("$.booksReadCount") { value(2) }
        }

      mockMvc
        .get("/api/v1/series/${series.id}/books")
        .andExpect {
          status { isOk() }
          jsonPath("$.content[0].readProgress.completed") { value(true) }
          jsonPath("$.content[1].readProgress.completed") { value(true) }
          jsonPath("$.numberOfElements") { value(2) }
        }
    }

    @Test
    @WithMockCustomUser(id = "1")
    fun `given user when marking series as unread then progress is removed for all books`() {
      val series =
        makeSeries(name = "series", libraryId = library.id).let { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1.cbr", libraryId = library.id), makeBook("2.cbr", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
            seriesLifecycle.sortBooks(created)
          }
        }

      bookRepository.findAll().forEach { book ->
        mediaRepository.findById(book.id).let { media ->
          mediaRepository.update(
            media.copy(
              status = Media.Status.READY,
              pages = (1..10).map { BookPage("$it", "image/jpeg") },
            ),
          )
        }
      }

      mockMvc
        .post("/api/v1/series/${series.id}/read-progress")
        .andExpect {
          status { isNoContent() }
        }

      mockMvc
        .delete("/api/v1/series/${series.id}/read-progress")
        .andExpect {
          status { isNoContent() }
        }

      mockMvc
        .get("/api/v1/series/${series.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.booksUnreadCount") { value(2) }
          jsonPath("$.booksReadCount") { value(0) }
        }

      mockMvc
        .get("/api/v1/series/${series.id}/books")
        .andExpect {
          status { isOk() }
          jsonPath("$.content[0].readProgress") { value(IsNull.nullValue()) }
          jsonPath("$.content[1].readProgress") { value(IsNull.nullValue()) }
          jsonPath("$.numberOfElements") { value(2) }
        }
    }

    @Test
    @WithMockCustomUser(id = "1")
    fun `given user when marking book as in progress then progress series return books count accordingly`() {
      val series =
        makeSeries(name = "series", libraryId = library.id).let { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books =
              listOf(
                makeBook("1.cbr", libraryId = library.id),
                makeBook("2.cbr", libraryId = library.id),
                makeBook("3.cbr", libraryId = library.id),
              )
            seriesLifecycle.addBooks(created, books)
            seriesLifecycle.sortBooks(created)
          }
        }

      bookRepository.findAll().forEach { book ->
        mediaRepository.findById(book.id).let { media ->
          mediaRepository.update(
            media.copy(
              status = Media.Status.READY,
              pages = (1..10).map { BookPage("$it", "image/jpeg") },
              pageCount = 10,
            ),
          )
        }
      }

      val books = bookRepository.findAll().sortedBy { it.name }

      mockMvc.patch("/api/v1/books/${books.elementAt(0).id}/read-progress") {
        contentType = MediaType.APPLICATION_JSON
        content = """{"page": 5,"completed":false}"""
      }
      mockMvc.patch("/api/v1/books/${books.elementAt(1).id}/read-progress") {
        contentType = MediaType.APPLICATION_JSON
        content = """{"completed":true}"""
      }

      mockMvc
        .get("/api/v1/series/${series.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.booksUnreadCount") { value(1) }
          jsonPath("$.booksReadCount") { value(1) }
          jsonPath("$.booksInProgressCount") { value(1) }
        }
    }
  }

  @Nested
  inner class FileDownload {
    @Test
    @WithMockCustomUser
    fun `given series with Unicode name when getting series file then attachment name is correct`() {
      val name = "アキラ"
      val tempFile =
        Files
          .createTempFile(name, ".cbz")
          .also { it.toFile().deleteOnExit() }
      val series =
        makeSeries(name = name, libraryId = library.id).let { series ->
          seriesLifecycle.createSeries(series).let { created ->
            val books = listOf(makeBook(name, libraryId = library.id, url = tempFile.toUri().toURL()))
            seriesLifecycle.addBooks(created, books)
          }
          series
        }

      mockMvc
        .get("/api/v1/series/${series.id}/file")
        .andExpect {
          status { isOk() }
          header { string("Content-Disposition", Matchers.containsString(URLEncoder.encode(name, StandardCharsets.UTF_8.name()))) }
        }
    }
  }

  @Nested
  inner class RecentSeries {
    @Test
    @WithMockCustomUser
    fun `given series that was just created when getting updated series then series is omitted`() {
      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(makeSeries(name = "series") to listOf(makeBook("book1").copy(fileSize = 1))).toScanResult(),
          mapOf(makeSeries(name = "series") to listOf(makeBook("book1").copy(fileSize = 2))).toScanResult(),
        )
      libraryContentLifecycle.scanRootFolder(library)

      mockMvc
        .get("/api/v1/series/updated")
        .andExpect {
          status { isOk() }
          jsonPath("$.content") { isEmpty() }
        }
    }
  }
}
