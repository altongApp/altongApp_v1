package com.example.altong_v2.ui.mypage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.altong_v2.databinding.ItemAlarmHistoryBinding

/* 알림 기록 RecyclerView Adapter */
class AlarmHistoryAdapter : ListAdapter<AlarmHistory, AlarmHistoryAdapter.ViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAlarmHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder( private val binding: ItemAlarmHistoryBinding ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(history: AlarmHistory) {
            // 상태 아이콘
            binding.tvStatusIcon.text = if (history.isCompleted) "✅" else "⏰"
            // 타이틀
            binding.tvTitle.text = history.getTitle()
            // 상세 정보
            binding.tvDetail.text = history.getDetail()
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<AlarmHistory>() {
        override fun areItemsTheSame(oldItem: AlarmHistory, newItem: AlarmHistory): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: AlarmHistory, newItem: AlarmHistory): Boolean {
            return oldItem == newItem
        }
    }
}