package com.example.altong_v2.ui.alarm

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.altong_v2.MainActivity
import com.example.altong_v2.R
import com.example.altong_v2.data.local.AppDatabase
import com.example.altong_v2.data.local.entity.MedicationLog
import com.example.altong_v2.data.repository.CalendarRepository
import com.example.altong_v2.databinding.FragmentAlarmConfirmBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AlarmConfirmFragment : Fragment() {
    private var _binding: FragmentAlarmConfirmBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: AppDatabase
    private lateinit var calendarRepository: CalendarRepository

    // Arguments로 받을 데이터
    private var prescriptionId: Long = 0L
    private var drugId: Long = 0L  // ✅ drugId 추가
    private var drugName: String = ""
    private var timeSlot: String = ""
    private var scheduledDate: Long = 0L

    companion object {
        private const val TAG = "AlarmConfirm"
        private const val ARG_PRESCRIPTION_ID = "prescription_id"
        private const val ARG_DRUG_ID = "drug_id"  // ✅ drugId 추가
        private const val ARG_DRUG_NAME = "drug_name"
        private const val ARG_TIME_SLOT = "time_slot"
        private const val ARG_SCHEDULED_DATE = "scheduled_date"

        fun newInstance(
            prescriptionId: Long,
            drugId: Long,  // ✅ drugId 추가
            drugName: String,
            timeSlot: String,
            scheduledDate: Long
        ): AlarmConfirmFragment {
            return AlarmConfirmFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_PRESCRIPTION_ID, prescriptionId)
                    putLong(ARG_DRUG_ID, drugId)  // ✅ drugId 추가
                    putString(ARG_DRUG_NAME, drugName)
                    putString(ARG_TIME_SLOT, timeSlot)
                    putLong(ARG_SCHEDULED_DATE, scheduledDate)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Arguments에서 데이터 받기
        arguments?.let {
            prescriptionId = it.getLong(ARG_PRESCRIPTION_ID)
            drugId = it.getLong(ARG_DRUG_ID)  // ✅ drugId 받기
            drugName = it.getString(ARG_DRUG_NAME) ?: ""
            timeSlot = it.getString(ARG_TIME_SLOT) ?: ""
            scheduledDate = it.getLong(ARG_SCHEDULED_DATE)
        }
        database = AppDatabase.getDatabase(requireContext())

        // ✅ CalendarRepository 초기화
        calendarRepository = CalendarRepository(
            drugDao = database.drugDao(),
            drugCompletionDao = database.drugCompletionDao(),
            prescriptionDao = database.prescriptionDao()
        )

        Log.d(TAG, "Fragment 생성: prescription=$prescriptionId, drugId=$drugId, drug=$drugName, timeSlot=$timeSlot")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlarmConfirmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        loadDrugInfo()
        setupClickListeners()
    }

    private fun setupUI() {
        // 시간대에 따라 헤더 텍스트 설정
        binding.tvTimeSlot.text = when(timeSlot) {
            "morning" -> "🌅 아침 약 드실 시간입니다!"
            "lunch" -> "☀️ 점심 약 드실 시간입니다!"
            "dinner" -> "🌙 저녁 약 드실 시간입니다!"
            "bedtime" -> "🛌 취침 전 약 드실 시간입니다!"
            else -> "💊 약 드실 시간입니다!"
        }
        // 현재 시간 표시
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        binding.tvCurrentTime.text = timeFormat.format(Date())
    }

    private fun loadDrugInfo() {
        lifecycleScope.launch {
            try {
                // 처방전 정보 가져오기
                val prescription = database.prescriptionDao().getPrescriptionById(prescriptionId)
                if (prescription == null) {
                    Log.e(TAG, "처방전을 찾을 수 없음: $prescriptionId")
                    showToast("처방전 정보를 찾을 수 없습니다")
                    return@launch
                }

                // 1. Flow에서 데이터를 한 번 꺼내 (.first() 사용)
                val drugs = database.drugDao().getDrugsByPrescription(prescriptionId).first()

                // 2. 시간대 영문 → 한글 변환
                val timeSlotKorean = when(timeSlot) {
                    "morning" -> "아침"
                    "lunch" -> "점심"
                    "dinner" -> "저녁"
                    "bedtime" -> "취침 전"
                    else -> timeSlot
                }

                Log.d(TAG, "시간대 변환: $timeSlot → $timeSlotKorean")

                // 3. 한글로 필터링
                val drugsForTimeSlot = drugs.filter { drug ->
                    Log.d(TAG, "약품: ${drug.name}, timeSlots: ${drug.timeSlots}")
                    drug.timeSlots?.contains(timeSlotKorean) == true  // ✅ 한글로 비교
                }

                Log.d(TAG, "로드된 약품 수: ${drugsForTimeSlot.size}")

                // 4. 약품 카드 동적 생성
                drugsForTimeSlot.forEach { drug ->
                    addDrugCard(prescription.diagnosis, drug)
                }

                if (drugsForTimeSlot.isEmpty()) {
                    showToast("이 시간대에 복용할 약이 없습니다")
                }

            } catch (e: Exception) {
                Log.e(TAG, "약품 정보 로드 실패", e)
                showToast("약품 정보를 불러오는 중 오류가 발생했습니다")
            }
        }
    }

    private fun addDrugCard(diagnosis: String, drug: com.example.altong_v2.data.local.entity.DrugEntity) {
        val cardView = layoutInflater.inflate(
            R.layout.item_alarm_drug,
            binding.llDrugList,
            false
        )

        // 진단명
        cardView.findViewById<TextView>(R.id.tv_diagnosis).text = "📋 $diagnosis"
        // 약품명
        cardView.findViewById<TextView>(R.id.tv_drug_name).text = "• ${drug.name}"
        // 복용 정보
        val info = buildString {
            append("1회 ${drug.dosage}, 1일 ${drug.frequency}")
            if (!drug.timing.isNullOrBlank()) {
                append(" / ${drug.timing}")
            }
        }
        cardView.findViewById<TextView>(R.id.tv_drug_info).text = info

        binding.llDrugList.addView(cardView)
    }

    private fun setupClickListeners() {
        // [예] 버튼
        binding.btnYes.setOnClickListener {
            Log.d(TAG, "복용 완료 버튼 클릭")
            markAsTaken()
        }
        // [아니오] 버튼
        binding.btnNo.setOnClickListener {
            Log.d(TAG, "아니오 버튼 클릭")
            showToast("빠르게 약을 챙겨 드세요! 💊")
            closeFragment()
        }
    }

    private fun markAsTaken() {
        lifecycleScope.launch {
            try {
                // 1. MedicationLog 저장 (기존 알림용)
                val existingLog = database.medicationLogDao().getLog(
                    prescriptionId = prescriptionId,
                    drugName = drugName,
                    timeSlot = timeSlot,
                    date = scheduledDate
                )

                if (existingLog != null) {
                    if (existingLog.taken) {
                        Log.d(TAG, "이미 복용 완료된 약품")
                        showToast("이미 복용 완료 처리되었습니다")
                    } else {
                        val updatedLog = existingLog.copy(
                            taken = true,
                            takenAt = Date()
                        )
                        database.medicationLogDao().update(updatedLog)
                        Log.d(TAG, "MedicationLog 업데이트 성공")
                    }
                } else {
                    val newLog = MedicationLog(
                        logId = 0,
                        prescriptionId = prescriptionId.toLong(),
                        drugName = drugName,
                        timeSlot = timeSlot,
                        scheduledDate = Date(scheduledDate),
                        taken = true,
                        takenAt = Date(),
                        createdAt = Date()
                    )
                    database.medicationLogDao().insert(newLog)
                    Log.d(TAG, "새 MedicationLog 생성 성공")
                }

                // ✅ 2. 캘린더에도 체크 (DrugCompletionEntity)
                syncToCalendar()

                showToast("복용 완료 처리되었습니다 ✅")
                closeFragment()

            } catch (e: Exception) {
                Log.e(TAG, "복용 완료 처리 실패", e)
                showToast("처리 중 오류가 발생했습니다")
            }
        }
    }

    /**
     * ✅ 캘린더와 동기화
     */
    private suspend fun syncToCalendar() {
        try {
            // 날짜 변환: Long → "yyyy-MM-dd"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateString = dateFormat.format(Date(scheduledDate))

            // 시간대 변환: "morning" → "아침"
            val timeSlotKorean = when(timeSlot) {
                "morning" -> "아침"
                "lunch" -> "점심"
                "dinner" -> "저녁"
                "bedtime" -> "취침 전"
                else -> timeSlot
            }

            // dateWithSlot: "2024-12-16-아침"
            val dateWithSlot = "$dateString-$timeSlotKorean"

            Log.d(TAG, "캘린더 동기화 시작:")
            Log.d(TAG, "  drugId=$drugId")
            Log.d(TAG, "  date=$dateString")
            Log.d(TAG, "  timeSlot=$timeSlot → $timeSlotKorean")
            Log.d(TAG, "  dateWithSlot=$dateWithSlot")

            // ✅ CalendarRepository로 체크 상태 저장
            calendarRepository.toggleCompletion(drugId, dateWithSlot)

            Log.d(TAG, "캘린더 동기화 성공!")

        } catch (e: Exception) {
            Log.e(TAG, "캘린더 동기화 실패", e)
            // 실패해도 알림 기록은 저장되었으므로 에러 무시
        }
    }

    private fun closeFragment() {
        Log.d(TAG, "closeFragment 호출")
        (activity as? MainActivity)?.navigateToHome()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}