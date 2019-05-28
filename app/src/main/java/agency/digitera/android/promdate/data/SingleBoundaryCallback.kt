package agency.digitera.android.promdate.data

import agency.digitera.android.promdate.util.ApiAccessor
import agency.digitera.android.promdate.util.PagingRequestHelper
import android.util.Log
import androidx.paging.PagedList
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executors

class SingleBoundaryCallback(private val db: SingleDb, private val token: String) :
    PagedList.BoundaryCallback<User>() {

    private val api = ApiAccessor()
    private val executor = Executors.newSingleThreadExecutor()
    private val helper = PagingRequestHelper(executor)

    var maxLoaded = 0

    override fun onZeroItemsLoaded() {
        super.onZeroItemsLoaded()

        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) { helperCallback ->
            api.apiService.getFeed(token, 3 * PAGE_SIZE) //TODO: Get token
                .enqueue(object : Callback<FeedResponse> {

                    override fun onFailure(call: Call<FeedResponse>?, t: Throwable) {
                        //3
                        Log.e("SingleBoundaryCallback", "Failed to load data!")
                        helperCallback.recordFailure(t)
                    }

                    override fun onResponse(
                        call: Call<FeedResponse>?,
                        response: Response<FeedResponse>
                    ) {
                        //4
                        val singles = response.body()?.result?.singles
                        executor.execute {
                            db.singleDao().insert(singles ?: listOf())
                            helperCallback.recordSuccess()
                        }
                        maxLoaded = 30
                    }
                })
        }
    }

    override fun onItemAtEndLoaded(itemAtEnd: User) {
        super.onItemAtEndLoaded(itemAtEnd)

        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) { helperCallback ->
            api.apiService.getFeed(token, PAGE_SIZE, singlesOffset = maxLoaded) //TODO: Fix
                .enqueue(object : Callback<FeedResponse> {

                    override fun onFailure(call: Call<FeedResponse>?, t: Throwable) {
                        Log.e("RedditBoundaryCallback", "Failed to load data!")
                        helperCallback.recordFailure(t)
                    }

                    override fun onResponse(
                        call: Call<FeedResponse>?,
                        response: Response<FeedResponse>) {

                        val singles = response.body()?.result?.singles
                        executor.execute {
                            db.singleDao().insert(singles ?: listOf())
                            helperCallback.recordSuccess()
                        }
                        maxLoaded += PAGE_SIZE
                    }
                })
        }
    }

    companion object {
        const val PAGE_SIZE = 10
    }
}