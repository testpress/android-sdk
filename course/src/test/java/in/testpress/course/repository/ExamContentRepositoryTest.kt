package `in`.testpress.course.repository

import `in`.testpress.core.TestpressException
import `in`.testpress.course.domain.asDomainLanguage
import `in`.testpress.course.enums.Status
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.network.NetworkAttempt
import `in`.testpress.course.network.NetworkContentAttempt
import `in`.testpress.course.network.Resource
import `in`.testpress.course.network.asGreenDaoModel
import `in`.testpress.course.util.RetrofitCallMock
import `in`.testpress.course.util.getOrAwaitValue
import `in`.testpress.exam.network.ExamNetwork
import `in`.testpress.exam.network.NetworkLanguage
import `in`.testpress.models.TestpressApiResponse
import `in`.testpress.models.greendao.Attempt
import `in`.testpress.models.greendao.AttemptDao
import `in`.testpress.models.greendao.CourseAttempt
import `in`.testpress.models.greendao.CourseAttemptDao
import `in`.testpress.models.greendao.Language
import `in`.testpress.models.greendao.LanguageDao
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.greenrobot.greendao.query.DeleteQuery
import org.greenrobot.greendao.query.QueryBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import org.powermock.api.mockito.PowerMockito.spy
import java.io.IOException

@RunWith(MockitoJUnitRunner::class)
class ExamContentRepositoryTest {
    private val courseNetwork = Mockito.mock(CourseNetwork::class.java)
    private val examNetwork = Mockito.mock(ExamNetwork::class.java)
    private val attemptDao = Mockito.mock(AttemptDao::class.java)
    private val contentAttemptDao = Mockito.mock(CourseAttemptDao::class.java)
    private val languageDao = Mockito.mock(LanguageDao::class.java)

    @Mock
    lateinit var queryBuilder: QueryBuilder<CourseAttempt>
    @Mock
    lateinit var languageQueryBuilder: QueryBuilder<Language>

    @Mock
    lateinit var deleteQuery: DeleteQuery<CourseAttempt>
    @Mock
    lateinit var lanuageDeleteQuery: DeleteQuery<Language>
    @Captor
    private val languageArgumentCaptor: ArgumentCaptor<List<Language>>? = null

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private var repository = ExamContentRepository(
        courseNetwork,
        examNetwork,
        contentAttemptDao,
        attemptDao,
        languageDao
    )

    @Before
    fun setUp() {
        repository = spy(repository)
        Mockito.`when`(contentAttemptDao.queryBuilder()).thenReturn(queryBuilder)
        Mockito.`when`(
            contentAttemptDao.queryBuilder().where(ArgumentMatchers.any(), ArgumentMatchers.any())
        ).thenReturn(queryBuilder)
        Mockito.`when`(contentAttemptDao.queryBuilder().buildDelete()).thenReturn(deleteQuery)
        Mockito.`when`(languageDao.queryBuilder()).thenReturn(languageQueryBuilder)
        Mockito.`when`(
            languageDao.queryBuilder().where(ArgumentMatchers.any(), ArgumentMatchers.any())
        ).thenReturn(languageQueryBuilder)
        Mockito.`when`(languageDao.queryBuilder().buildDelete()).thenReturn(lanuageDeleteQuery)

    }

    fun createContentAttempt(): NetworkContentAttempt {
        val assessment = NetworkAttempt(2)
        return NetworkContentAttempt(1, assessment = assessment)
    }

    fun createContentAttemptResponse(): TestpressApiResponse<NetworkContentAttempt> {
        val response = TestpressApiResponse<NetworkContentAttempt>()
        response.results.add(createContentAttempt())
        return response
    }

    @Test
    fun loadAttemptsShouldMakeApiCall() {
        val apiCall =
            RetrofitCallMock(Resource.success(TestpressApiResponse<NetworkContentAttempt>()))
        Mockito.`when`(courseNetwork.getContentAttempts(anyString())).thenReturn(apiCall)
        repository.loadAttempts("attempts_url", 1)

        verify(courseNetwork).getContentAttempts("attempts_url")
        verify(repository).clearContentAttemptsInDB(1)
        verify(repository).saveCourseAttemptInDB(arrayListOf(), 1)
    }

    @Test
    fun onLoadAttemptsSuccessLiveDataShouldBeUpdatedAndAttemptsShouldBeStored() {
        val contentAttemptApiResponse = createContentAttemptResponse()
        val apiCall = RetrofitCallMock(Resource.success(contentAttemptApiResponse))
        Mockito.`when`(courseNetwork.getContentAttempts(anyString())).thenReturn(apiCall)
        val result = repository.loadAttempts("attempts_url", 1).getOrAwaitValue()

        assert(Status.SUCCESS == result.status)
        assert(contentAttemptApiResponse.results == result.data)
        verify(repository).clearContentAttemptsInDB(1)
        verify(repository).saveCourseAttemptInDB(ArrayList(contentAttemptApiResponse.results), 1)
    }

    @Test
    fun onLoadAttemptsFailureLiveDataShouldBeErrorResource() {
        val apiCall = RetrofitCallMock(
            Resource.error(
                TestpressException.networkError(IOException()),
                createContentAttemptResponse()
            )
        )
        Mockito.`when`(courseNetwork.getContentAttempts(anyString())).thenReturn(apiCall)
        val result = repository.loadAttempts("attempts_url", 1).getOrAwaitValue()

        assert(Status.ERROR == result.status)
        assert(apiCall.resource.exception == result.exception)
    }

    @Test
    fun saveCourseAttemptInDBShouldSaveCourseAttemptsInDB() {
        val contentAttempt = createContentAttempt()
        repository.saveCourseAttemptInDB(arrayListOf(contentAttempt), 1)
        val argumentCaptor = ArgumentCaptor.forClass(Attempt::class.java)
        val contentAttemptArgumentCaptor = ArgumentCaptor.forClass(CourseAttempt::class.java)

        verify(attemptDao).insertOrReplace(argumentCaptor.capture())
        verify(contentAttemptDao).insertOrReplace(contentAttemptArgumentCaptor.capture())

        val storedAttempt = argumentCaptor.allValues[0]
        assert(contentAttempt.assessment?.id == storedAttempt.id)

        val storedContentAttempt = contentAttemptArgumentCaptor.allValues[0]
        assert(1L == storedContentAttempt.chapterContentId)
        assert(storedAttempt.id == storedContentAttempt.assessmentId)
    }

    @Test
    fun getContentAttemptsFromDB() {
        val contentAttemptList = listOf(createContentAttempt().asGreenDaoModel())
        Mockito.`when`(contentAttemptDao.queryBuilder().list()).thenReturn(contentAttemptList)
        val result = repository.getContentAttempts(1)

        assert(contentAttemptList[0].id == result[0].id)
    }

    @Test
    fun fetchLanguagesShouldMakeApiCall() {
        val apiCall =
            RetrofitCallMock(Resource.success(TestpressApiResponse<NetworkLanguage>()))
        Mockito.`when`(examNetwork.getLanguages(anyString())).thenReturn(apiCall)
        repository.loadLanguages("slug", 2)

        verify(examNetwork).getLanguages("slug")
    }

    @Test
    fun onFetchLanguagesSuccessItShouldBeStored() {
        val networkLanguageResponse = TestpressApiResponse<NetworkLanguage>()
        networkLanguageResponse.results.add(NetworkLanguage(1))
        val apiCall =
            RetrofitCallMock(Resource.success(networkLanguageResponse))
        Mockito.`when`(examNetwork.getLanguages(anyString())).thenReturn(apiCall)
        val result = repository.loadLanguages("slug", 1).getOrAwaitValue()

        assert(Status.SUCCESS==result.status)
        assert(networkLanguageResponse.results[0].asDomainLanguage() == result.data!![0])
        verify(repository).storeLanguagesInDB(ArrayList(networkLanguageResponse.results), 1)
    }

    @Test
    fun onFetchFailureExceptionShouldBeStoredInLivedata() {
        val apiCall = RetrofitCallMock(
            Resource.error(
                TestpressException.networkError(IOException()),
                TestpressApiResponse<NetworkLanguage>()
            )
        )
        Mockito.`when`(examNetwork.getLanguages(anyString())).thenReturn(apiCall)
        val result = repository.loadLanguages("slug", 1).getOrAwaitValue()

        assert(Status.ERROR==result.status)
        assert(apiCall.resource.exception==result.exception)
    }


    @Test
    fun storeLanguagesInDBShouldStoreLanguagesInDB() {
        val networkLanguage = NetworkLanguage(1)
        repository.storeLanguagesInDB(arrayListOf(networkLanguage), 5)

        verify(languageDao).insertOrReplaceInTx(languageArgumentCaptor!!.capture())
        val languageStored = languageArgumentCaptor.allValues[0][0]
        assert(5L==languageStored.examId)
    }
}