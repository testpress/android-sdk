package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.network.NetworkHighlight
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.HighlightEntity
import `in`.testpress.v2_4.models.ApiResponse
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.HashMap

class HighlightRepository(private val context: Context) {
    private val courseNetwork = CourseNetwork(context)
    private val database = TestpressDatabase.invoke(context.applicationContext)
    private val highlightDao = database.highlightDao()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun getHighlights(
        contentId: Long,
        callback: TestpressCallback<ApiResponse<List<NetworkHighlight>>>
    ) {
        courseNetwork.getHighlights(contentId).enqueue(callback)
    }

    fun createHighlight(
        contentId: Long,
        highlight: HashMap<String, Any>,
        callback: TestpressCallback<NetworkHighlight>
    ) {
        courseNetwork.createHighlight(contentId, highlight).enqueue(object : TestpressCallback<NetworkHighlight>() {
            override fun onSuccess(response: NetworkHighlight) {
                scope.launch {
                    try {
                        response.id?.let { id ->
                            val entity = HighlightEntity(
                                id = id,
                                contentId = contentId,
                                pageNumber = response.pageNumber,
                                selectedText = response.selectedText,
                                notes = response.notes,
                                color = response.color,
                                position = response.position,
                                created = response.created,
                                modified = response.modified
                            )
                            highlightDao.insert(entity)
                        }
                    } catch (e: Exception) {
                        Log.e(
                            "HighlightRepository",
                            "Failed to cache created highlight in database",
                            e
                        )
                    }
                }
                callback.onSuccess(response)
            }

            override fun onException(exception: TestpressException?) {
                callback.onException(exception)
            }
        })
    }

    fun deleteHighlight(
        contentId: Long,
        highlightId: Long,
        callback: TestpressCallback<Void>
    ) {
        courseNetwork.deleteHighlight(contentId, highlightId).enqueue(object : TestpressCallback<Void>() {
            override fun onSuccess(response: Void?) {
                scope.launch {
                    try {
                        highlightDao.deleteById(highlightId)
                    } catch (e: Exception) {
                        Log.e(
                            "HighlightRepository",
                            "Failed to delete highlight with id=$highlightId from database",
                            e
                        )
                    }
                }
                callback.onSuccess(response)
            }

            override fun onException(exception: TestpressException?) {
                callback.onException(exception)
            }
        })
    }

    suspend fun getCachedHighlights(contentId: Long): List<NetworkHighlight> {
        return withContext(Dispatchers.IO) {
            try {
                highlightDao
                    .getHighlightsByContent(contentId)
                    .map { it.toNetworkHighlight() }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    fun fetchHighlights(
        contentId: Long,
        onSuccess: (List<NetworkHighlight>) -> Unit,
        onException: ((TestpressException?) -> Unit)? = null
    ) {
        scope.launch {
            try {
                val cachedHighlights = highlightDao.getHighlightsByContent(contentId)
                if (cachedHighlights.isNotEmpty()) {
                    val networkHighlights = cachedHighlights.map { it.toNetworkHighlight() }
                    withContext(Dispatchers.Main) {
                        onSuccess(networkHighlights)
                    }
                }
            } catch (e: Exception) {
                Log.e(
                    "HighlightRepository",
                    "Failed to load cached highlights for contentId=$contentId",
                    e
                )
            }
        }

        courseNetwork.getHighlights(contentId).enqueue(object : TestpressCallback<ApiResponse<List<NetworkHighlight>>>() {
            override fun onSuccess(response: ApiResponse<List<NetworkHighlight>>) {
                scope.launch {
                    try {
                        val highlights = response.results ?: emptyList()
                        highlightDao.deleteByContent(contentId)
                        val entities = highlights.mapNotNull { highlight ->
                            highlight.id?.let { id ->
                                HighlightEntity(
                                    id = id,
                                    contentId = contentId,
                                    pageNumber = highlight.pageNumber,
                                    selectedText = highlight.selectedText,
                                    notes = highlight.notes,
                                    color = highlight.color,
                                    position = highlight.position,
                                    created = highlight.created,
                                    modified = highlight.modified
                                )
                            }
                        }
                        if (entities.isNotEmpty()) {
                            highlightDao.insertAll(entities)
                        }
                    } catch (e: Exception) {
                        Log.e(
                            "HighlightRepository",
                            "Failed to update local cache after network highlights fetch for contentId=$contentId",
                            e
                        )
                    }
                }
                onSuccess(response.results ?: emptyList())
            }

            override fun onException(exception: TestpressException?) {
                onException?.invoke(exception)
            }
        })
    }

    private fun HighlightEntity.toNetworkHighlight(): NetworkHighlight {
        return NetworkHighlight(
            id = this.id,
            pageNumber = this.pageNumber,
            selectedText = this.selectedText,
            notes = this.notes,
            color = this.color,
            position = this.position,
            created = this.created,
            modified = this.modified
        )
    }
}

