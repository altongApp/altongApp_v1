package com.example.altong_v2.ui.prescription

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView


/* * ScrollView 안에서 모든 아이템을 표시하는 RecyclerView
* 참고 - 처방상세에서 약이 1개이상 안보이는 문제때문에 생성해봄
* 실행결과 이걸로 해결됨. */

class WrapContentRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        // 높이를 무제한으로 설정
        val expandSpec = MeasureSpec.makeMeasureSpec(
            Int.MAX_VALUE shr 2,
            MeasureSpec.AT_MOST
        )
        super.onMeasure(widthSpec, expandSpec)
    }
}