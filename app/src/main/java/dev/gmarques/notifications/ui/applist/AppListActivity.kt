package dev.gmarques.notifications.ui.applist

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.SearchView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.notifications.R
import dev.gmarques.notifications.databinding.ActivityAppListBinding
import dev.gmarques.notifications.domain.model.AppInfo
import dev.gmarques.notifications.ui.configuration.ConfigurationActivity
import dev.gmarques.notifications.ui.viewmodel.AppListViewModel

@AndroidEntryPoint
class AppListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppListBinding
    private val viewModel: AppListViewModel by viewModels()
    private lateinit var adapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        observeViewModel()

        viewModel.loadInstalledApps()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.select_app)
        }
    }

    private fun setupRecyclerView() {
        adapter = AppListAdapter { appInfo:AppInfo ->
            openConfigurationScreen(appInfo.packageName)
        }

        binding.recyclerViewApps.apply {
            layoutManager = LinearLayoutManager(this@AppListActivity)
            adapter = this@AppListActivity.adapter
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText)
                return true
            }
        })
    }

    private fun observeViewModel() {
        viewModel.installedApps.observe(this) { apps ->
            adapter.submitList(apps)
            adapter.setOriginalList(apps)
            updateEmptyState(apps.isEmpty())
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.recyclerViewApps.isVisible = !isLoading
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.layoutEmptyState.isVisible = isEmpty && !binding.progressBar.isVisible
    }

    private fun openConfigurationScreen(packageName: String) {
        val intent = Intent(this, ConfigurationActivity::class.java).apply {
            putExtra(ConfigurationActivity.EXTRA_PACKAGE_NAME, packageName)
        }
        startActivity(intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}