package dev.gmarques.notifications.ui.configuration

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.notifications.R
import dev.gmarques.notifications.databinding.ActivityConfigurationBinding
import dev.gmarques.notifications.domain.model.ListType
import dev.gmarques.notifications.ui.viewmodel.ConfigurationViewModel
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@AndroidEntryPoint
class ConfigurationActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
    }

    private lateinit var binding: ActivityConfigurationBinding
    private val viewModel: ConfigurationViewModel by viewModels()
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigurationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
        if (packageName == null) {
            Toast.makeText(this, R.string.error_invalid_package, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupDayChips()
        setupTimeSelectors()
        setupListTypeSwitch()
        setupSaveButton()
        observeViewModel()

        viewModel.loadAppInfo(packageName)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.configure_notifications)
        }
    }

    private fun setupDayChips() {
        DayOfWeek.entries.forEach { day ->
            val chip = Chip(this).apply {
                text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        viewModel.toggleDay(day)
                    } else {
                        viewModel.toggleDay(day)
                    }
                }
            }
            binding.chipGroupDays.addView(chip)
        }
    }

    private fun updateDayChips(selectedDays: Set<DayOfWeek>) {
        Log.d(
            "USUK",
            "ConfigurationActivity.".plus("updateDayChips() selectedDays = $selectedDays")
        )
           for (i in 0 until binding.chipGroupDays.childCount) {
            val chip = binding.chipGroupDays.getChildAt(i) as Chip
            val day: DayOfWeek = DayOfWeek.entries[i]
            chip.isChecked = selectedDays.contains(day)
        }
    }

    private fun setupTimeSelectors() {
        binding.layoutStartTime.setOnClickListener {
            showTimePicker(isStartTime = true)
        }

        binding.layoutEndTime.setOnClickListener {
            showTimePicker(isStartTime = false)
        }
    }

    private fun showTimePicker(isStartTime: Boolean) {
        val currentTime = if (isStartTime) {
            viewModel.startTime.value ?: LocalTime.of(8, 0)
        } else {
            viewModel.endTime.value ?: LocalTime.of(18, 0)
        }

        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(currentTime.hour)
            .setMinute(currentTime.minute)
            .setTitleText(
                if (isStartTime) R.string.select_start_time else R.string.select_end_time
            )
            .build()

        picker.addOnPositiveButtonClickListener {
            val newTime = LocalTime.of(picker.hour, picker.minute)
            if (isStartTime) {
                viewModel.setStartTime(newTime)
            } else {
                viewModel.setEndTime(newTime)
            }
        }

        picker.show(supportFragmentManager, "TimePicker")
    }

    private fun setupListTypeSwitch() {
        binding.switchListType.setOnCheckedChangeListener { _, isChecked ->
            val listType = if (isChecked) ListType.WHITELIST else ListType.BLACKLIST
            viewModel.setListType(listType)
            updateListTypeDescription(listType)
        }
    }

    private fun updateListTypeDescription(listType: ListType) {
        val descriptionRes = when (listType) {
            ListType.BLACKLIST -> R.string.blacklist_description
            ListType.WHITELIST -> R.string.whitelist_description
        }
        binding.textListTypeDescription.setText(descriptionRes)
    }

    private fun setupSaveButton() {
        binding.buttonSave.setOnClickListener {
            viewModel.saveConfiguration()
            Toast.makeText(this, R.string.configuration_saved, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.appInfo.observe(this) { appInfo ->
            Log.d("USUK", "ConfigurationActivity.observeViewModel 0: $appInfo")
            binding.imageAppIcon.setImageDrawable(appInfo.appIcon)
            binding.textAppName.text = appInfo.appName
        }

        viewModel.selectedDays.observe(this) { days -> //loop aqui
            updateDayChips(days)
        }

        viewModel.startTime.observe(this) { time ->
            binding.textStartTime.text = time.format(timeFormatter)
        }

        viewModel.endTime.observe(this) { time ->
            binding.textEndTime.text = time.format(timeFormatter)
        }

        viewModel.listType.observe(this) { listType ->
            binding.switchListType.isChecked = listType == ListType.WHITELIST
            updateListTypeDescription(listType)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}