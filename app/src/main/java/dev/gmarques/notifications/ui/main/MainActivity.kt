package dev.gmarques.notifications.ui.main

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.notifications.R
import dev.gmarques.notifications.databinding.ActivityMainBinding
import dev.gmarques.notifications.domain.model.AppConfiguration
import dev.gmarques.notifications.domain.model.AppInfo
import dev.gmarques.notifications.domain.service.MyNotificationForegroundService
import dev.gmarques.notifications.ui.applist.AppListActivity
import dev.gmarques.notifications.ui.blockednotifications.BlockedNotificationsActivity
import dev.gmarques.notifications.ui.configuration.ConfigurationActivity
import dev.gmarques.notifications.ui.viewmodel.MainViewModel
import dev.gmarques.notifications.utils.isNotificationListenerEnabled
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: ManagedAppsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupClickListeners()

        lifecycleScope.launch {
            viewModel.managedApps.collect { apps ->
                adapter.submitList(apps)
                updateEmptyState(apps.isEmpty())
            }
        }

        lifecycleScope.launch {
            viewModel.appInfoMap.collect { appInfoMap ->
                adapter.updateAppInfoMap(appInfoMap)
            }
        }

        // Verifica permissão de acesso a notificações
        checkNotificationListenerPermission()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadManagedApps()
    }

    private fun setupRecyclerView() {
        adapter = ManagedAppsAdapter(
            onItemClick = { appConfig ->
                openConfigurationScreen(appConfig.packageName)
            },
            onViewBlockedClick = { packageName ->
                openBlockedNotificationsScreen(packageName)
            }
        )

        binding.recyclerViewApps.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAddApp.setOnClickListener {
            val intent = Intent(this, AppListActivity::class.java)
            startActivity(intent)
        }

    }

    private fun openConfigurationScreen(packageName: String) {
        val intent = Intent(this, ConfigurationActivity::class.java).apply {
            putExtra(ConfigurationActivity.EXTRA_PACKAGE_NAME, packageName)
        }
        startActivity(intent)
    }

    private fun openBlockedNotificationsScreen(packageName: String) {
        val intent = Intent(this, BlockedNotificationsActivity::class.java).apply {
            putExtra(BlockedNotificationsActivity.EXTRA_PACKAGE_NAME, packageName)
        }
        startActivity(intent)
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.recyclerViewApps.visibility = android.view.View.GONE
            binding.layoutEmptyState.visibility = android.view.View.VISIBLE
        } else {
            binding.recyclerViewApps.visibility = android.view.View.VISIBLE
            binding.layoutEmptyState.visibility = android.view.View.GONE
        }
    }

    private fun checkNotificationListenerPermission() {
        if (!isNotificationListenerEnabled(this)) {
            showNotificationPermissionDialog()
        } else {
            // Inicia o service em primeiro plano
            MyNotificationForegroundService.Starter.startService(this)
        }
    }

    private fun showNotificationPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_required)
            .setMessage(R.string.notification_permission_explanation)
            .setPositiveButton(R.string.allow_access) { _, _ ->
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                startActivity(intent)
            }
            .setCancelable(false)
            .show()
    }
}

// Adaptador para lista de aplicativos gerenciados
class ManagedAppsAdapter(
    private val onItemClick: (AppConfiguration) -> Unit,
    private val onViewBlockedClick: (String) -> Unit
) : androidx.recyclerview.widget.ListAdapter<AppConfiguration, ManagedAppsAdapter.ViewHolder>(
    object : androidx.recyclerview.widget.DiffUtil.ItemCallback<AppConfiguration>() {
        override fun areItemsTheSame(oldItem: AppConfiguration, newItem: AppConfiguration) =
            oldItem.packageName == newItem.packageName

        override fun areContentsTheSame(oldItem: AppConfiguration, newItem: AppConfiguration) =
            oldItem == newItem
    }
) {

    private var appInfoMap: Map<String, AppInfo> = emptyMap()

    fun updateAppInfoMap(map: Map<String, AppInfo>) {
        appInfoMap = map
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val binding = dev.gmarques.notifications.databinding.ItemManagedAppBinding.inflate(
            android.view.LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appConfig = getItem(position)
        val appInfo = appInfoMap[appConfig.packageName]
        holder.bind(appConfig, appInfo)
    }

    inner class ViewHolder(
        private val binding: dev.gmarques.notifications.databinding.ItemManagedAppBinding
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bind(appConfig: AppConfiguration, appInfo: AppInfo?) {
            appInfo?.let {
                binding.imageAppIcon.setImageDrawable(it.appIcon)
                binding.textAppName.text = it.appName
            }

            val context = binding.root.context
            val daysText = if (appConfig.scheduledDays.isEmpty()) {
                context.getString(R.string.no_days_selected)
            } else {
                appConfig.scheduledDays.joinToString(", ") {
                    it.name.lowercase(Locale.ROOT).capitalize()
                }
            }

            binding.textSchedule.text = context.getString(
                R.string.schedule_format,
                daysText,
                appConfig.startTime.toString(),
                appConfig.endTime.toString()
            )

            val listTypeText = when(appConfig.listType) {
                dev.gmarques.notifications.domain.model.ListType.BLACKLIST ->
                    context.getString(R.string.blacklist)
                dev.gmarques.notifications.domain.model.ListType.WHITELIST ->
                    context.getString(R.string.whitelist)
            }
            binding.textListType.text = listTypeText

            binding.root.setOnClickListener {
                onItemClick(appConfig)
            }

            binding.buttonViewBlocked.setOnClickListener {
                onViewBlockedClick(appConfig.packageName)
            }
        }
    }
}

