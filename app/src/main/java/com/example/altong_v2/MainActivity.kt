package com.example.altong_v2

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.altong_v2.databinding.ActivityMainBinding
import com.example.altong_v2.ui.calendar.CalendarFragment
import com.example.altong_v2.ui.medicine.MedicineFragment
import com.example.altong_v2.ui.mypage.MyPageFragment
import com.example.altong_v2.ui.prescription.PrescriptionFragment

/*

 * MainActivity 에서는
 * 하단 네비게이션을 통한 Fragment 전환 관리
 * - 나의 약통 (PrescriptionFragment)
 * - 약 검색 (MedicineFragment)
 * - 캘린더 (CalendarFragment)
 * - 마이페이지 (MyPageFragment)
*/


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomNavigation()
        if (savedInstanceState == null) {
            replaceFragment(PrescriptionFragment())
        }
    }
    /*        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }*/

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
}