package com.sedo.contextmenu.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.sedo.contextmenu.R
import com.sedo.contextmenu.data.models.Menu
import com.sedo.contextmenu.databinding.DialogContextMenuBinding
import com.sedo.contextmenu.ui.adapters.ContextMenuRecyclerAdapter
import com.sedo.contextmenu.ui.base.BaseBindingRecyclerViewAdapter
import com.sedo.contextmenu.utils.*
import com.sedo.contextmenu.utils.extensions.setOnItemClickListener


class ContextDialog(
        private val mContext: Context,
        private val viewGroup: ViewGroup,
        private val view: View,
        private val items: List<Menu>,
        private val contextDialogCallBack: ContextDialogCallBack
) :
        Dialog(mContext, R.style.FullScreenTransparentDialog) {

    var selectedPosition = 0
    var viewIndex = 0
    var selectedItem: Menu? = null
    var layoutParams: RelativeLayout.LayoutParams? = null
    private lateinit var dialogContextMenuBinding: DialogContextMenuBinding
    lateinit var contextMenuRecyclerAdapter: ContextMenuRecyclerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogContextMenuBinding =
                DataBindingUtil.inflate(
                        LayoutInflater.from(context),
                        R.layout.dialog_context_menu,
                        null,
                        false
                )
        setUpView()
        setContentView(dialogContextMenuBinding.root)
        setCancelable(true)
        setUpBinding()
        setUpListeners()
        setUpMenuRecyclerView()
    }

    private fun setUpListeners() {
        setOnDismissListener {
            dialogContextMenuBinding?.viewContainer?.removeView(view)
            if (viewGroup !is RecyclerView) {
                if (viewGroup is LinearLayout) {
                    viewGroup.addView(view, viewIndex)
                } else if (viewGroup is RelativeLayout) {
                    viewGroup.addView(view, layoutParams)
                } else {
                    viewGroup.addView(view)
                }
                animateView()
                enableDisableView(true)
            }
            contextDialogCallBack.returned(selectedItem, selectedPosition)
        }
    }

    private fun setUpBinding() {
        dialogContextMenuBinding?.viewModel = this
    }

    private fun setUpView() {
        if (viewGroup is LinearLayout)
            viewIndex = viewGroup.indexOfChild(view)
        else if (viewGroup is RelativeLayout)
            layoutParams = view.layoutParams as RelativeLayout.LayoutParams
        viewGroup.removeView(view)
        enableDisableView(false)
        val insertPoint = dialogContextMenuBinding?.viewContainer as ViewGroup
        insertPoint.addView(
                view,
                0,
                ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        )
        view.setOnLongClickListener {
            return@setOnLongClickListener false
        }
        animateView()
    }

    private fun animateView() {
        val animation: Animation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        animation.startOffset = 50.toLong()
        view.startAnimation(animation)
    }

    private fun enableDisableView(enable: Boolean) {
        if (view is ViewGroup)
            if (enable)
                view.enableViews()
            else
                view.disableViews()
        else
            if (enable)
                view.enableView()
            else
                view.disableView()
    }

    private fun setUpMenuRecyclerView() {
        contextMenuRecyclerAdapter = ContextMenuRecyclerAdapter(context)
        dialogContextMenuBinding?.recyclerView?.adapter = contextMenuRecyclerAdapter
        dialogContextMenuBinding?.recyclerView?.setOnItemClickListener(object :
                BaseBindingRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(view1: View?, position: Int, item: Any) {
                selectedItem = item as Menu
                selectedPosition = position
                dismiss()
            }

        })
        dialogContextMenuBinding?.recyclerView?.addItemDecoration(
                DividerItemDecorator(
                        context.resources.getDrawable(R.drawable.divider), 0, 0
                )
        )
        contextMenuRecyclerAdapter.submitItems(items)
    }

    interface ContextDialogCallBack {
        fun returned(item: Menu?, position: Int)
    }

}
