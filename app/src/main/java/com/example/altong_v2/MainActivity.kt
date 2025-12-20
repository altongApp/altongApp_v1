package com.example.altong_v2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.altong_v2.databinding.ActivityMainBinding
import com.example.altong_v2.ui.alarm.AlarmConfirmFragment
import com.example.altong_v2.ui.alarm.NotificationHelper
import com.example.altong_v2.ui.calendar.CalendarFragment
import com.example.altong_v2.ui.medicine.MedicineFragment
import com.example.altong_v2.ui.mypage.MyPageFragment
import com.example.altong_v2.ui.prescription.PrescriptionFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val TAG = "MainActivity"
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "✅ 알림 권한 승인됨")
        } else {
            Log.e(TAG, "❌ 알림 권한 거부됨")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "========================================")
        Log.d(TAG, "MainActivity onCreate 호출!!")
        Log.d(TAG, "Intent: $intent")
        Log.d(TAG, "Intent extras: ${intent?.extras?.keySet()?.joinToString()}")
        Log.d(TAG, "========================================")
        Log.d(TAG, "onCreate 호출")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomNavigation()

        checkAndRequestNotificationPermission()

        val handledAlarm = handleAlarmIntent(intent)

        // 알림에서 온 게 아닐 때만 기본 Fragment 표시
        if (savedInstanceState == null && !handledAlarm) {
            Log.d(TAG, "기본 Fragment 표시: PrescriptionFragment")
            replaceFragment(PrescriptionFragment())
        }
    }

    //알림 권한 체크 및 요청
    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "✅ 알림 권한 이미 있음")
                }
                else -> {
                    Log.d(TAG, "⚠️ 알림 권한 요청")
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAlarmIntent(intent)
    }

    // 하단 네비 클릭리스너 설정
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_prescription -> {
                    replaceFragment(PrescriptionFragment())
                    true
                }

                R.id.nav_medicine -> {
                    replaceFragment(MedicineFragment())
                    true
                }

                R.id.nav_calendar -> {
                    replaceFragment(CalendarFragment())
                    true
                }

                R.id.nav_mypage -> {
                    replaceFragment(MyPageFragment())
                    true
                }

                else -> false
            }
        }
    }

    // 프래그먼트 교체
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // 알림 Intent 처리
    private fun handleAlarmIntent(intent: android.content.Intent?): Boolean {
        Log.d(TAG, "========================================")
        Log.d(TAG, "handleAlarmIntent 호출")

        if (intent == null) {
            Log.d(TAG, "Intent is null")
            Log.d(TAG, "========================================")
            return false
        }
        val showAlarmConfirm = intent.getBooleanExtra(
            NotificationHelper.EXTRA_SHOW_ALARM_CONFIRM,
            false
        )
        Log.d(TAG, "showAlarmConfirm: $showAlarmConfirm")

        if (showAlarmConfirm) {
            val prescriptionId = intent.getLongExtra(
                NotificationHelper.EXTRA_PRESCRIPTION_ID,
                0L
            )
            val drugId = intent.getLongExtra(
                NotificationHelper.EXTRA_DRUG_ID,  //drugId 추가
                0L
            )
            val drugName = intent.getStringExtra(
                NotificationHelper.EXTRA_DRUG_NAME
            ) ?: ""
            val timeSlot = intent.getStringExtra(
                NotificationHelper.EXTRA_TIME_SLOT
            ) ?: ""
            val scheduledDate = intent.getLongExtra(
                NotificationHelper.EXTRA_SCHEDULED_DATE,
                System.currentTimeMillis()
            )
            Log.d(TAG, "알림 데이터:")
            Log.d(TAG, "  prescriptionId: $prescriptionId")
            Log.d(TAG, "  drugId: $drugId")
            Log.d(TAG, "  drugName: $drugName")
            Log.d(TAG, "  timeSlot: $timeSlot")
            Log.d(TAG, "  scheduledDate: $scheduledDate")

            // AlarmConfirmFragment 표시
            Log.d(TAG, "AlarmConfirmFragment 표시 시작")
            val fragment = AlarmConfirmFragment.newInstance(
                prescriptionId = prescriptionId,
                drugId = drugId,  // drugId 전달
                drugName = drugName,
                timeSlot = timeSlot,
                scheduledDate = scheduledDate
            )
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("AlarmConfirm")
                .commit()
            return true  // 알림 처리함
        }
        return false  // 알림 처리 안함
    }

    fun navigateToHome() {
        Log.d(TAG, "navigateToHome 호출")
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, PrescriptionFragment())
            .commit()
    }
}