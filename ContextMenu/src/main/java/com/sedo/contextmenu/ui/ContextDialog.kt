package com.sedo.contextmenu.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.sedo.contextmenu.R
import com.sedo.contextmenu.data.models.CustomData
import com.sedo.contextmenu.data.models.Menu
import com.sedo.contextmenu.databinding.DialogContextMenuBinding
import com.sedo.contextmenu.ui.adapters.ContextMenuRecyclerAdapter
import com.sedo.contextmenu.ui.base.BaseBindingRecyclerViewAdapter
import com.sedo.contextmenu.utils.*
import com.sedo.contextmenu.utils.binidngadapters.loadImage
import com.sedo.contextmenu.utils.extensions.setOnItemClickListener


class ContextDialog private constructor(
    private val builder: Builder
) : Dialog(builder.context, R.style.FullScreenTransparentDialog) {

    private var selectedPosition = 0
    private var viewIndex = 0
    private var selectedItem: Menu? = null
    private var layoutParams: RelativeLayout.LayoutParams? = null
    private lateinit var dialogContextMenuBinding: DialogContextMenuBinding
    private lateinit var contextMenuRecyclerAdapter: ContextMenuRecyclerAdapter

    private var viewGroup: ViewGroup? = null
    private var view: View? = null
    private var items: MutableList<Menu> = mutableListOf()
    private var callBack: ContextDialogCallBack? = null
    private var customViewResId: Int? = null
    private var customData: CustomData? = null

    init {
        with(builder) {
            viewGroup = getViewGroup()
            view = getView()
            items = getItems()
            callBack = getCallBack()
            customViewResId = getCustomViewResId()
            customData = getCustomData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setUpBindingView()
        setUpBinding()
        setUpViews()
    }

    private fun setUpBindingView() {
        dialogContextMenuBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_context_menu,
                null,
                false
            )
        setContentView(dialogContextMenuBinding.root)
        setCancelable(true)
        setUpBinding()
        setUpListeners()
        setUpListeners()
        setUpMenuRecyclerView()
    }

    private fun setUpBinding() {
        dialogContextMenuBinding.viewModel = this
    }

    private fun isCustomView(): Boolean {
        return (customData != null)
    }

    private fun setUpViews() {
        if (isCustomView()) {
            setUpCustomView()
        } else {
            removeFromGroupView()
        }
    }

    private fun setUpCustomView() {
        view = LayoutInflater.from(context)
            .inflate(customViewResId ?: R.layout.layout_custom_view, null, false)
        view?.findViewById<ImageView>(R.id.imgPhoto)?.apply {
            customData?.image?.let {
                loadImage(image = it)
            } ?: also {
                gone()
            }
        }
        view?.findViewById<TextView>(R.id.tvTitle)?.apply {
            customData?.title?.let {
                text = it
            } ?: also {
                gone()
            }
            customData?.titleColor?.let {
                context.resources.getColor(it).let {
                    setTextColor(it)
                }
            }
        }
        view?.findViewById<TextView>(R.id.tvSubTitle)?.apply {
            customData?.subtitle?.let {
                text = it
            } ?: also {
                gone()
            }
            customData?.subtitleColor?.let {
                context.resources.getColor(it).let {
                    setTextColor(it)
                }
            }
        }
        view?.findViewById<TextView>(R.id.tvDate)?.apply {
            customData?.date?.let {
                text = it
            } ?: also {
                gone()
            }
            customData?.dateColor?.let {
                context.resources.getColor(it).let {
                    setTextColor(it)
                }
            }
        }
        customData?.backgroundColor?.let {
            context.resources.getColor(it).let {
                view?.findViewById<CardView>(R.id.cvRoot)?.setCardBackgroundColor(it)
            }
        }
        view?.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        addView()
    }

    private fun setUpListeners() {
        setOnDismissListener {
            try {
                dialogContextMenuBinding.viewContainer.removeView(view)
                if (viewGroup !is RecyclerView) {
                    when (viewGroup) {
                        is LinearLayout -> {
                            viewGroup?.addView(view, viewIndex)
                        }
                        is RelativeLayout -> {
                            viewGroup?.addView(view, layoutParams)
                        }
                        else -> {
                            viewGroup?.addView(view)
                        }
                    }
                    animateView()
                    enableDisablePopView(true)
                }
                callBack?.returned(selectedItem, selectedPosition)
            } catch (e: Exception) {
                Log.d("Context Menu", e.localizedMessage)
            }
        }
    }

    private fun animateView() {
        val animation: Animation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        animation.startOffset = 50.toLong()
        view?.startAnimation(animation)
    }


    private fun removeFromGroupView() {
        try {
            if (viewGroup is LinearLayout)
                viewIndex = viewGroup?.indexOfChild(view) ?: 0
            else if (viewGroup is RelativeLayout)
                layoutParams = view?.layoutParams as RelativeLayout.LayoutParams
            viewGroup?.removeView(view)
            enableDisablePopView(false)
            addView()
            if (viewGroup is RecyclerView)
                view?.setOnLongClickListener {
                    return@setOnLongClickListener false
                }
            animateView()
        } catch (e: Exception) {
            Log.d("Context Menu", e.localizedMessage)
        }
    }

    private fun addView() {
        val insertPoint = dialogContextMenuBinding.viewContainer as ViewGroup
        insertPoint.addView(
            view,
            0
        )
    }

    private fun enableDisablePopView(enable: Boolean) {
        if (view is ViewGroup)
            if (enable)
                view?.enableViews()
            else
                view?.disableViews()
        else
            if (enable)
                view?.enableView()
            else
                view?.disableView()
    }

    private fun setUpMenuRecyclerView() {
        contextMenuRecyclerAdapter = ContextMenuRecyclerAdapter(context)
        dialogContextMenuBinding.recyclerView.adapter = contextMenuRecyclerAdapter
        dialogContextMenuBinding.recyclerView.setOnItemClickListener(object :
            BaseBindingRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, position: Int, item: Any) {
                selectedItem = item as Menu
                selectedPosition = position
                dismiss()
            }

        })
        dialogContextMenuBinding.recyclerView.addItemDecoration(
            DividerItemDecorator(
                context.resources.getDrawable(R.drawable.divider), 0, 0
            )
        )
        contextMenuRecyclerAdapter.submitItems(items)
    }

    interface ContextDialogCallBack {
        fun returned(item: Menu?, position: Int)
    }

    class Builder(val context: Context) {
        private var viewGroup: ViewGroup? = null
        private var view: View? = null
        private var items: MutableList<Menu> = mutableListOf()
        private var callBack: ContextDialogCallBack? = null
        private var customViewResId: Int? = null
        private var customData: CustomData? = null
        fun setViewGroup(viewGroup: ViewGroup): Builder {
            this.viewGroup = viewGroup
            return this
        }

        fun getViewGroup(): ViewGroup? {
            return viewGroup
        }

        fun setView(view: View): Builder {
            this.view = view
            return this
        }

        fun getView(): View? {
            return view
        }

        fun setItems(items: MutableList<Menu>): Builder {
            this.items = items
            return this
        }

        fun getItems(): MutableList<Menu> {
            return items
        }

        fun setCallBack(callBack: ContextDialogCallBack): Builder {
            this.callBack = callBack
            return this
        }

        fun getCallBack(): ContextDialogCallBack? {
            return callBack
        }

        fun setCustomResId(customViewResId: Int?): Builder {
            this.customViewResId = customViewResId
            return this
        }

        fun getCustomViewResId(): Int? {
            return customViewResId
        }

        fun setCustomData(customData: CustomData?): Builder {
            this.customData = customData
            return this
        }

        fun getCustomData(): CustomData? {
            return customData
        }

        fun build(): ContextDialog {
            return ContextDialog(this)
        }
    }

}
