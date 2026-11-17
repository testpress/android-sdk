package `in`.testpress.course.repository

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.course.util.GreendaoCleanupMixin
import `in`.testpress.course.util.RetrofitCallMock
import `in`.testpress.course.util.getOrAwaitValue
import `in`.testpress.enums.Status
import `in`.testpress.exam.domain.DomainQuestion
import `in`.testpress.exam.domain.DomainUserSelectedAnswer
import `in`.testpress.exam.domain.getResolvedExplanation
import `in`.testpress.exam.network.ExamNetwork
import `in`.testpress.exam.network.NetworkExamQuestionResult
import `in`.testpress.models.greendao.DaoSession
import `in`.testpress.models.greendao.ExamQuestion
import `in`.testpress.models.greendao.ExamQuestionDao
import `in`.testpress.models.greendao.UserSelectedAnswer
import `in`.testpress.models.greendao.UserSelectedAnswerDao
import `in`.testpress.network.Resource
import `in`.testpress.v2_4.models.ApiResponse
import org.greenrobot.greendao.query.QueryBuilder
import org.greenrobot.greendao.query.WhereCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import java.io.IOException

@RunWith(MockitoJUnitRunner::class)
class QuizQuestionsRepositoryTest : GreendaoCleanupMixin() {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var daoSession: DaoSession

    @Mock
    lateinit var examQuestionDao: ExamQuestionDao

    @Mock
    lateinit var userSelectedAnswerDao: UserSelectedAnswerDao

    @Mock
    lateinit var userSelectedAnswerQueryBuilder: QueryBuilder<UserSelectedAnswer>

    @Mock
    lateinit var examQuestionQueryBuilder: QueryBuilder<ExamQuestion>

    @Mock
    lateinit var examNetwork: ExamNetwork

    private lateinit var repository: QuizQuestionsRepository

    @Before
    fun setUp() {
        setStaticField(TestpressSDKDatabase::class.java, "daoSession", daoSession)
        Mockito.`when`(daoSession.examQuestionDao).thenReturn(examQuestionDao)
        Mockito.`when`(daoSession.userSelectedAnswerDao).thenReturn(userSelectedAnswerDao)
        Mockito.`when`(userSelectedAnswerDao.queryBuilder()).thenReturn(userSelectedAnswerQueryBuilder)
        Mockito.`when`(userSelectedAnswerQueryBuilder.where(any(WhereCondition::class.java))).thenReturn(userSelectedAnswerQueryBuilder)
        Mockito.`when`(examQuestionDao.queryBuilder()).thenReturn(examQuestionQueryBuilder)
        Mockito.`when`(examQuestionQueryBuilder.where(any(WhereCondition::class.java))).thenReturn(examQuestionQueryBuilder)

        repository = QuizQuestionsRepository(context)
        setField(repository, "examNetwork", examNetwork)
    }

    private fun setStaticField(clazz: Class<*>, fieldName: String, value: Any?) {
        val field = clazz.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(null, value)
    }

    private fun setField(target: Any, fieldName: String, value: Any?) {
        val field = target.javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(target, value)
    }

    @Test
    fun duplicateInFlightGetQuestionsCallsForSameAttemptIdAreIgnored() {
        Mockito.`when`(userSelectedAnswerQueryBuilder.list()).thenReturn(emptyList())

        val apiCall = RetrofitCallMock(Resource.success(ApiResponse<NetworkExamQuestionResult>()))
        Mockito.`when`(examNetwork.getQuestions(anyString(), anyMap())).thenReturn(apiCall)

        repository.getQuestions(1L, 100L, "http://testpress.in/api/questions/")
        repository.getQuestions(1L, 100L, "http://testpress.in/api/questions/")

        verify(examNetwork, times(1)).getQuestions(anyString(), anyMap())
    }

    @Test
    fun offlineFallbackIsUsedWhenNetworkFetchFailsAndQuestionsExistInDB() {
        Mockito.`when`(userSelectedAnswerQueryBuilder.list()).thenReturn(emptyList())
        val exception = TestpressException.networkError(IOException("No internet"))
        val apiCall = RetrofitCallMock(Resource.error(exception, ApiResponse<NetworkExamQuestionResult>()))
        Mockito.`when`(examNetwork.getQuestions(anyString(), anyMap())).thenReturn(apiCall)

        val cachedQuestion = ExamQuestion(1L)
        Mockito.`when`(examQuestionQueryBuilder.list()).thenReturn(listOf(cachedQuestion))

        repository.getQuestions(1L, 100L, "http://testpress.in/api/questions/")

        verify(examQuestionDao, Mockito.atLeastOnce()).queryBuilder()
    }

    @Test
    fun offlineFallbackIsUsedWhenFetchSucceedsWithEmptyResultsAndCacheExists() {
        Mockito.`when`(userSelectedAnswerQueryBuilder.list()).thenReturn(emptyList())
        val emptyResponse = ApiResponse<NetworkExamQuestionResult>()
        val apiCall = RetrofitCallMock(Resource.success(emptyResponse))
        Mockito.`when`(examNetwork.getQuestions(anyString(), anyMap())).thenReturn(apiCall)

        val cachedQuestion = ExamQuestion(1L)
        Mockito.`when`(examQuestionQueryBuilder.list()).thenReturn(listOf(cachedQuestion))

        repository.getQuestions(1L, 100L, "http://testpress.in/api/questions/")

        verify(examQuestionQueryBuilder, Mockito.never()).buildDelete()
        verify(examQuestionDao, Mockito.atLeastOnce()).queryBuilder()
    }

    @Test
    fun errorResourceEmittedWhenFetchSucceedsWithNoQuestions() {
        Mockito.`when`(userSelectedAnswerQueryBuilder.list()).thenReturn(emptyList())
        val emptyResponse = ApiResponse<NetworkExamQuestionResult>()
        val apiCall = RetrofitCallMock(Resource.success(emptyResponse))
        Mockito.`when`(examNetwork.getQuestions(anyString(), anyMap())).thenReturn(apiCall)

        Mockito.`when`(examQuestionQueryBuilder.list()).thenReturn(emptyList())

        val result = repository.getQuestions(1L, 100L, "http://testpress.in/api/questions/").getOrAwaitValue()

        assertEquals(Status.ERROR, result.status)
        assertEquals("No questions found", result.exception?.cause?.message)
    }

    @Test
    fun errorResourceEmittedWhenExceptionOccursInSuccessPath() {
        Mockito.`when`(userSelectedAnswerQueryBuilder.list()).thenReturn(emptyList())
        val emptyResponse = ApiResponse<NetworkExamQuestionResult>()
        val apiCall = RetrofitCallMock(Resource.success(emptyResponse))
        Mockito.`when`(examNetwork.getQuestions(anyString(), anyMap())).thenReturn(apiCall)

        Mockito.`when`(examQuestionQueryBuilder.list()).thenThrow(RuntimeException("Database failure"))

        val result = repository.getQuestions(1L, 100L, "http://testpress.in/api/questions/").getOrAwaitValue()

        assertEquals(Status.ERROR, result.status)
        assertEquals("Database failure", result.exception?.cause?.message)
    }

    @Test
    fun consistentExplanationResolutionAcrossSubmitAndReview() {
        val answerWithExplanation = DomainUserSelectedAnswer(
            explanationHtml = "Authoritative explanation",
            question = DomainQuestion(explanation = "Question explanation")
        )
        val answerWithFallbackOnly = DomainUserSelectedAnswer(
            explanationHtml = null,
            question = DomainQuestion(explanation = "Question explanation")
        )

        assertEquals("Authoritative explanation", answerWithExplanation.getResolvedExplanation())
        assertEquals("Question explanation", answerWithFallbackOnly.getResolvedExplanation())
    }
}
