package agency.digitera.android.promdate.ui

import androidx.lifecycle.Observer
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import agency.digitera.android.promdate.*
import agency.digitera.android.promdate.adapters.SingleAdapter
import agency.digitera.android.promdate.data.SingleBoundaryCallback
import agency.digitera.android.promdate.data.SingleDb
import agency.digitera.android.promdate.data.User
import kotlinx.android.synthetic.main.fragment_scrollable_tab.*
import java.util.concurrent.Executors




class SinglesTabFragment : Fragment(), TabInterface {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: SingleAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var liveData: LiveData<PagedList<User>>
    private lateinit var database: SingleDb

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_scrollable_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //sets up recycler view
        viewManager = LinearLayoutManager(context)
        viewAdapter = SingleAdapter {
            onUserClick(it) //sets onClick function for each item in the list
        }
        recyclerView = view.findViewById<RecyclerView>(R.id.user_recycler).apply {
            layoutManager = viewManager
            itemAnimator = DefaultItemAnimator()
            adapter = viewAdapter
        }

        //set up swipe to refresh
        swipe_refresh.setOnRefreshListener {
            invalidate()
        }

        database = SingleDb.create(context!!)

        initializeList()
    }

    private fun initializeList() {
        val config = PagedList.Config.Builder()
            .setPageSize(8)
            .setEnablePlaceholders(false)
            .build()

        liveData = initializedPagedListBuilder(config).build()

        liveData.observe(this, Observer<PagedList<User>> { pagedList ->
            viewAdapter.submitList(pagedList)
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val lastKey = viewAdapter.currentList?.lastKey as Int

        outState.putInt("lastKey", lastKey)
        outState.putParcelable("layout_manager_state", recyclerView.layoutManager?.onSaveInstanceState())
    }

    override fun invalidate() {
        if (!this::liveData.isInitialized) {
            initializeList()
        } else {
            val executor = Executors.newSingleThreadExecutor()
            executor.execute {
                database.singleDao().clearDatabase()
                activity?.runOnUiThread {
                    initializeList()
                    swipe_refresh.isRefreshing = false
                }
            }
        }
    }

    private fun initializedPagedListBuilder(config: PagedList.Config): LivePagedListBuilder<Int, User> {
        val livePageListBuilder = LivePagedListBuilder<Int, User>(
            database.singleDao().singles(),
            config)

        //get token
        val sp: SharedPreferences =
            context?.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                ?: throw BadTokenException() //TODO: Return to login
        val token = sp.getString("token", null) ?: ""

        livePageListBuilder.setBoundaryCallback(SingleBoundaryCallback(database, token))
        return livePageListBuilder
    }

    private fun onUserClick(user: User) {
        //open user profile
        val action = FeedFragmentDirections.navProfile(
            user.id,
            0,
            user.firstName + " " + user.lastName
        )
        findNavController().navigate(action)
    }
}