package com.example.altong_v2.ui.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.altong_v2.data.model.DrugItem
import com.example.altong_v2.databinding.ItemDrugCalendarBinding
import com.example.altong_v2.databinding.ItemTimeSlotHeaderBinding

/**
 * 약 리스트 어댑터
 * 시간대(아침, 점심, 저녁, 취침 전)별로 약을 표시
 *
 * ViewType 2개:
 * - TIME_SLOT_HEADER: 시간대 헤더 (예: "🌅 아침 08:00")
 * - DRUG_ITEM: 개별 약 아이템
 */
class DrugCalendarAdapter(
    private val onDrugCheckChanged: (Long) -> Unit  // 약 체크박스 클릭 콜백
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // ViewType 상수
    companion object {
        private const val TYPE_TIME_SLOT_HEADER = 0
        private const val TYPE_DRUG_ITEM = 1

        // 시간대별 이모지와 시간 매핑 (하드코딩 - 추후 마이페이지 설정으로 변경 가능)
        private val TIME_SLOT_INFO = mapOf(
            "아침" to Pair("🌅", "08:00"),
            "점심" to Pair("☀️", "12:00"),
            "저녁" to Pair("🌆", "18:00"),
            "취침 전" to Pair("🌙", "22:00")
        )
    }

    // 표시할 아이템 리스트 (헤더 + 약들)
    private val displayItems = mutableListOf<DisplayItem>()

    /**
     * 표시할 아이템 타입
     * - TimeSlotHeader: 시간대 헤더
     * - DrugItem: 개별 약
     */
    sealed class DisplayItem {
        data class TimeSlotHeader(val timeSlot: String) : DisplayItem()
        data class Drug(val drugItem: DrugItem) : DisplayItem()
    }

    /**
     * 데이터 업데이트
     * @param drugsByTimeSlot 시간대별 약 리스트 Map
     */
    fun submitList(drugsByTimeSlot: Map<String, List<DrugItem>>) {
        displayItems.clear()

        // 🔍 디버깅 로그
        android.util.Log.d("DrugAdapter", """
            ========================================
            submitList 호출됨!
            받은 시간대 개수: ${drugsByTimeSlot.size}
            시간대 키: ${drugsByTimeSlot.keys.joinToString()}
        """.trimIndent())

        // 시간대 순서 정의 (아침 → 점심 → 저녁 → 취침 전)
        val timeSlotOrder = listOf("아침", "점심", "저녁", "취침 전")

        // 시간대별로 헤더와 약들 추가
        for (timeSlot in timeSlotOrder) {
            val drugs = drugsByTimeSlot[timeSlot]
            android.util.Log.d("DrugAdapter", "  체크: '$timeSlot' -> ${drugs?.size ?: 0}개 약")

            if (drugs != null && drugs.isNotEmpty()) {
                // 시간대 헤더 추가
                displayItems.add(DisplayItem.TimeSlotHeader(timeSlot))
                android.util.Log.d("DrugAdapter", "    → 헤더 추가: $timeSlot")

                // 해당 시간대의 약들 추가
                drugs.forEach { drug ->
                    displayItems.add(DisplayItem.Drug(drug))
                    android.util.Log.d("DrugAdapter", "    → 약 추가: ${drug.drugName}")
                }
            }
        }

        android.util.Log.d("DrugAdapter", "최종 displayItems 개수: ${displayItems.size}")
        android.util.Log.d("DrugAdapter", "========================================")

        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (displayItems[position]) {
            is DisplayItem.TimeSlotHeader -> TYPE_TIME_SLOT_HEADER
            is DisplayItem.Drug -> TYPE_DRUG_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TIME_SLOT_HEADER -> {
                val binding = ItemTimeSlotHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                TimeSlotHeaderViewHolder(binding)
            }
            TYPE_DRUG_ITEM -> {
                val binding = ItemDrugCalendarBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                DrugViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = displayItems[position]) {
            is DisplayItem.TimeSlotHeader -> {
                (holder as TimeSlotHeaderViewHolder).bind(item.timeSlot)
            }
            is DisplayItem.Drug -> {
                (holder as DrugViewHolder).bind(item.drugItem)
            }
        }
    }

    override fun getItemCount(): Int = displayItems.size

    /**
     * 시간대 헤더 ViewHolder
     */
    class TimeSlotHeaderViewHolder(
        private val binding: ItemTimeSlotHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(timeSlot: String) {
            val (emoji, time) = TIME_SLOT_INFO[timeSlot] ?: Pair("⏰", "00:00")
            binding.tvTimeSlot.text = "$emoji $timeSlot $time"
        }
    }

    /**
     * 약 아이템 ViewHolder
     */
    inner class DrugViewHolder(
        private val binding: ItemDrugCalendarBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(drug: DrugItem) {
            // 약 이름
            binding.tvDrugName.text = drug.drugName

            // 복용량
            binding.tvDosage.text = drug.dosage

            // 복용 시점 (있으면 표시)
            if (!drug.timing.isNullOrEmpty()) {
                binding.tvTiming.text = drug.timing
                binding.tvTiming.visibility = View.VISIBLE
            } else {
                binding.tvTiming.visibility = View.GONE
            }

            // 남은 일수 (0보다 크면 표시)
            if (drug.remainingDays > 0) {
                binding.tvRemainingDays.text = "(남은 약: ${drug.remainingDays}일분)"
                binding.tvRemainingDays.visibility = View.VISIBLE
            } else {
                binding.tvRemainingDays.visibility = View.GONE
            }

            // 체크박스 상태
            binding.cbDrug.isChecked = drug.isCompleted

            // 체크박스 클릭 리스너
            binding.cbDrug.setOnClickListener {
                onDrugCheckChanged(drug.drugId)
            }

            // 아이템 전체 클릭 시 체크박스 토글
            binding.root.setOnClickListener {
                binding.cbDrug.isChecked = !binding.cbDrug.isChecked
                onDrugCheckChanged(drug.drugId)
            }
        }
    }
}