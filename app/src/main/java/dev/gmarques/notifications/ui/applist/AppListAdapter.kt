package dev.gmarques.notifications.ui.applist

import android.view.View
import dev.gmarques.notifications.domain.model.AppInfo

class AppListAdapter(private val onItemClick: (AppInfo) -> Unit) :
    androidx.recyclerview.widget.ListAdapter<AppInfo, AppListAdapter.ViewHolder>(
        object : androidx.recyclerview.widget.DiffUtil.ItemCallback<AppInfo>() {
            override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo) =
                oldItem.packageName == newItem.packageName

            override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo) =
                oldItem == newItem
        }
    ) {

    private var originalList = listOf<AppInfo>()

    fun setOriginalList(list: List<AppInfo>) {
        originalList = list
    }

    fun filter(query: String?) {
        if (query.isNullOrBlank()) {
            submitList(originalList)
            return
        }

        val filteredList = originalList.filter {
            it.appName.contains(query, ignoreCase = true)
        }
        submitList(filteredList)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val binding = dev.gmarques.notifications.databinding.ItemAppBinding.inflate(
            android.view.LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: dev.gmarques.notifications.databinding.ItemAppBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bind(app: AppInfo) {
            binding.imageAppIcon.setImageDrawable(app.appIcon)
            binding.textAppName.text = app.appName

            if (app.isManaged) {
                binding.textManaged.visibility = View.VISIBLE
            } else {
                binding.textManaged.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                onItemClick(app)
            }
        }
    }
}
