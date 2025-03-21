package dev.gmarques.notifications.ui.blockednotifications

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.notifications.R
import dev.gmarques.notifications.databinding.ActivityBlockedNotificationsBinding
import dev.gmarques.notifications.domain.model.BlockedNotification
import dev.gmarques.notifications.ui.viewmodel.BlockedNotificationsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class BlockedNotificationsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
    }

    private lateinit var binding: ActivityBlockedNotificationsBinding
    private val viewModel: BlockedNotificationsViewModel by viewModels()
    private lateinit var adapter: BlockedNotificationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockedNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
        if (packageName == null) {
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        setupClearAllButton()
        observeViewModel()

        viewModel.loadBlockedNotifications(packageName)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.blocked_notifications)
        }
    }

    private fun setupRecyclerView() {
        adapter = BlockedNotificationsAdapter { notification ->
            showRemoveNotificationDialog(notification)
        }

        binding.recyclerViewNotifications.apply {
            layoutManager = LinearLayoutManager(this@BlockedNotificationsActivity)
            adapter = this@BlockedNotificationsActivity.adapter
        }
    }

    private fun setupClearAllButton() {
        binding.buttonClearAll.setOnClickListener {
            showClearAllDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.appInfo.observe(this) { appInfo ->
            binding.textAppName.text = getString(R.string.blocked_for_app, appInfo.appName)
            binding.imageAppIcon.setImageDrawable(appInfo.appIcon)
        }

        viewModel.blockedNotifications.observe(this) { notifications ->
            adapter.submitList(notifications)
            updateEmptyState(notifications.isEmpty())
            binding.buttonClearAll.isVisible = notifications.isNotEmpty()
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.layoutEmptyState.isVisible = isEmpty
        binding.recyclerViewNotifications.isVisible = !isEmpty
    }

    private fun showRemoveNotificationDialog(notification: BlockedNotification) {
        AlertDialog.Builder(this)
            .setTitle(R.string.remove_notification)
            .setMessage(R.string.remove_notification_confirm)
            .setPositiveButton(R.string.remove) { _, _ ->
                viewModel.removeBlockedNotification(notification.packageName, notification.id)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showClearAllDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.clear_all_notifications)
            .setMessage(R.string.clear_all_notifications_confirm)
            .setPositiveButton(R.string.clear_all) { _, _ ->
                viewModel.appInfo.value?.let { appInfo ->
                    viewModel.clearAllBlockedNotifications(appInfo.packageName)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
