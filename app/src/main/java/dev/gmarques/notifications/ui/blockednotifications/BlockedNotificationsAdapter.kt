package dev.gmarques.notifications.ui.blockednotifications

import dev.gmarques.notifications.domain.model.BlockedNotification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BlockedNotificationsAdapter(
    private val onItemLongClick: (BlockedNotification) -> Unit
) : androidx.recyclerview.widget.ListAdapter<BlockedNotification, BlockedNotificationsAdapter.ViewHolder>(
    object : androidx.recyclerview.widget.DiffUtil.ItemCallback<BlockedNotification>() {
        override fun areItemsTheSame(oldItem: BlockedNotification, newItem: BlockedNotification) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: BlockedNotification, newItem: BlockedNotification) =
            oldItem == newItem
    }
) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val binding = dev.gmarques.notifications.databinding.ItemBlockedNotificationBinding.inflate(
            android.view.LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: dev.gmarques.notifications.databinding.ItemBlockedNotificationBinding
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: BlockedNotification) {
            binding.textTitle.text = notification.title
            binding.textContent.text = notification.content

            val date = Date(notification.timestamp)
            binding.textTimestamp.text = dateFormat.format(date)

            binding.root.setOnLongClickListener {
                onItemLongClick(notification)
                true
            }
        }
    }
}
