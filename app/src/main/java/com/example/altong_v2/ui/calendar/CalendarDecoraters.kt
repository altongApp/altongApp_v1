package com.example.altong_v2.ui.calendar

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.text.style.ForegroundColorSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan

/*
 * 약이 있는 날짜에 연두색 점 표시
 * @param drugDates 약이 있는 날짜들의 Set
 * @param color 점 색상
 */
class DrugDatesDecorator(
    private val drugDates: Set<CalendarDay>,
    private val color: Int
) : DayViewDecorator {

    // 이 날짜를 꾸며야 하는지 판단
    // @return true면 이 날짜에 데코레이션 적용
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return drugDates.contains(day)
    }

    // 날짜 꾸미기 - 연두색 점을 추가
    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(8f, color))  // 반지름 8dp의 점
    }
}

/*
 * 선택된 날짜에 분홍색 배경 + 분홍색 점 표시
 *
 * @param selectedDate 선택된 날짜
 * @param backgroundColor 배경 색상
 * @param textColor 텍스트 색상
 */
class SelectedDateDecorator(
    private val selectedDate: CalendarDay?,
    private val backgroundColor: Int,
    private val textColor: Int
) : DayViewDecorator {


     // 선택된 날짜인지 판단
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day == selectedDate
    }


     // 분홍색 배경과 텍스트 색상 적용
    override fun decorate(view: DayViewFacade) {
        view.setBackgroundDrawable(createCircleDrawable(backgroundColor))
        view.addSpan(ForegroundColorSpan(textColor))
        // 분홍색 점도 추가 (배경과 동일한 색)
        view.addSpan(DotSpan(8f, backgroundColor))
    }


     // 원형 배경 Drawable 생성
    private fun createCircleDrawable(color: Int): Drawable {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.OVAL
        drawable.setColor(color)
        return drawable
    }
}

/*
 * 오늘 날짜 강조 (선택되지 않았을 때)
 *
 * @param today 오늘 날짜
 * @param color 테두리 색상
 */
class TodayDecorator(
    private val today: CalendarDay,
    private val color: Int
) : DayViewDecorator {

    //오늘 날짜인지 판단
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day == today
    }

    // 날짜 테두리만 표시 (배경은 투명)
    override fun decorate(view: DayViewFacade) {
        view.setBackgroundDrawable(createCircleBorderDrawable(color))
        view.addSpan(ForegroundColorSpan(Color.BLACK)) // 텍스트는 무조건 검정
    }

    // 원형 테두리 Drawable 생성
    private fun createCircleBorderDrawable(color: Int): Drawable {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.OVAL
        drawable.setStroke(4, color)  // 4px 두께의 테두리
        drawable.setColor(Color.TRANSPARENT)  // 배경은 투명!
        return drawable
    }
}

/*
 * 기본 날짜 데코레이터 (선택되지 않은 날짜들)
*/
class DefaultDayDecorator(
    private val selectedDate: CalendarDay?,
    private val today: CalendarDay
) : DayViewDecorator {

    // 선택되지 않은 날짜인지 판단
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day != selectedDate
    }

    // 텍스트 색상을 검정으로 설정
    override fun decorate(view: DayViewFacade) {
        // 선택되지 않은 날짜는 텍스트를 검정으로
        view.addSpan(ForegroundColorSpan(Color.BLACK))
    }
}