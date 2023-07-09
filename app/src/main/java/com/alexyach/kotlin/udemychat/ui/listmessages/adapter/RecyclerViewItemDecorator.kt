package com.alexyach.kotlin.udemychat.ui.listmessages.adapter

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.alexyach.kotlin.udemychat.R

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