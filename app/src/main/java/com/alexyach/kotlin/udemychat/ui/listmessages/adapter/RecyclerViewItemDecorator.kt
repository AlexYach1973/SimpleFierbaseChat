package com.alexyach.kotlin.udemychat.ui.listmessages.adapter

import android.graphics.Rect
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.TEXT_ALIGNMENT_TEXT_END
import androidx.recyclerview.widget.RecyclerView.TEXT_ALIGNMENT_VIEW_START
import com.alexyach.kotlin.udemychat.R
import com.alexyach.kotlin.udemychat.utils.LOG_TAG
import kotlin.math.absoluteValue

class RecyclerViewItemDecorator: RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {

//        Log.d(LOG_TAG, "getItemOffsets, view: ${view} ")

        if (view.id == R.id.myMessageItem) {
            outRect.set(250, 0, 0, 0)

        } else {
            outRect.set(0, 0, 250, 0)
        }
    }
}