package com.sedo.contextmenu.ui

import android.app.Activity
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
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
import com.sedo.contextmenu.utils.extensions.blur
import com.sedo.contextmenu.utils.extensions.setOnItemClickListener


class ContextDialog private constructor(
    private val builder: Builder
) : Dialog(builder.context, R.style.FullScreenTransparentDialog) {

    private var selectedPosition = 0
    private var viewIndex = 0
    private var selectedItem: Menu? = null
    private var layoutParams: RelativeLayout.LayoutParams? = null
    private lateinit var binding: DialogContextMenuBinding
    private lateinit var contextMenuRecyclerAdapter: ContextMenuRecyclerAdapter
    private var context : Activity = builder.context
    private var viewGroup: ViewGroup? = null
    private var view: View? = null
    private var items: MutableList<Menu> = mutableListOf()
    private var callBack: ContextDialogCallBack? = null
    private var customViewResId: Int? = null
    private var customData: CustomData? = null
    private var cornerRadius: Float? = null
    private var fillWidth: Boolean? = null
    private var height: Int? = null
    private var width: Int? = null
    private var blur: Float? = null
    private var backgroundColor: Int? = null

    init {
        with(builder) {
            view = getView()
            view?.parent?.let {
                it as ViewGroup
                viewGroup = it
            }
            items = getItems()
            callBack = getCallBack()
            customViewResId = getCustomViewResId()
            customData = getCustomData()
            fillWidth = getFillWidth()
            width = getWidth()
            height = getHeight()
            blur = getBlur()
            backgroundColor = getBackgroundColor()
            cornerRadius = getCornerRadius()
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
        binding =
            DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_context_menu,
                null,
                false
            )
        setContentView(binding.root)
        setCancelable(true)
        setUpBinding()
        setUpMenuRecyclerView()
    }

    private fun setUpBinding() {
        binding.viewModel = this
    }

    private fun isCustomView(): Boolean {
        return (customData != null)
    }

    private fun setUpViews() {
        binding.cvContent.let { cvContent ->
            cornerRadius?.let {
                cvContent.radius = it
            }
            backgroundColor?.let {
                context.resources.getColor(it).let {
                    cvContent.setCardBackgroundColor(it)
                }
            }
        }

        if (isCustomView()) {
            setUpCustomView()
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                removeFromGroupView()
            }, 0)
        }
        setUpListeners()
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
        view?.findViewById<CardView>(R.id.cvRoot)?.let { cvRoot ->
            customData?.backgroundColor?.let {
                context.resources.getColor(it).let {
                    cvRoot.setCardBackgroundColor(it)
                }
            }
            customData?.cornerRadius?.let {
                cvRoot.radius = it
            }
        }
        var params = view?.layoutParams
        if (params == null) {
            view?.layoutParams = LinearLayout.LayoutParams(
                width ?: LinearLayout.LayoutParams.MATCH_PARENT,
                height ?: LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        addView()
    }

    private fun setUpListeners() {
        setOnDismissListener {
            try {
                binding.viewContainer.removeView(view)
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
        setBackgroundBlur()
        view?.setOnClickListener {
            callBack?.rootViewClicked(it)
        }
    }

    private fun setBackgroundBlur() {
        // First get bitmap with blur filter applied, using the function blur presented here,
        // or another function.
        // Activity parameter is the Activity for which you call dialog.show();

        val bitmap: Bitmap? = context.blur(blur)

        // Get bitmap height.
        bitmap?.let {
            val bitmapHeight = bitmap.height
            setOnShowListener { dialogInterface ->
                // When dialog is shown, before set new blurred image for background drawable,
                // the root view height and dialog margin are saved.
                val rootViewHeight: Int = binding.root.height ?: 0
                val marginLeftAndRight: Int = window?.decorView?.paddingLeft ?: 0

                // Set new blurred image for background drawable.
                window?.setBackgroundDrawable(BitmapDrawable(context.resources, bitmap))

                // After get positions and heights, recover and rebuild original marginTop position,
                // that is (bitmapHeight - rootViewHeight) / 2.
                // Also recover and rebuild Dialog original left and right margin.
                val rootViewLayoutParams = binding.root.layoutParams as FrameLayout.LayoutParams
                rootViewLayoutParams.setMargins(
                    marginLeftAndRight,
                    (bitmapHeight - rootViewHeight) / 2,
                    marginLeftAndRight,
                    0
                )
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
        val insertPoint = binding.viewContainer as ViewGroup
        var params = view?.layoutParams
        if (params == null) {
            params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        if (fillWidth != null && fillWidth == true) {
            params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        insertPoint.addView(
            view,
            0,
            params
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
        binding.recyclerView.adapter = contextMenuRecyclerAdapter
        binding.recyclerView.setOnItemClickListener(object :
            BaseBindingRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, position: Int, item: Any) {
                selectedItem = item as Menu
                selectedPosition = position
                dismiss()
            }

        })
        binding.recyclerView.addItemDecoration(
            DividerItemDecorator(
                context.resources.getDrawable(R.drawable.divider), 0, 0
            )
        )
        contextMenuRecyclerAdapter.submitItems(items)
    }

    interface ContextDialogCallBack {
        fun returned(item: Menu?, position: Int) {}
        fun rootViewClicked(view: View) {}
    }

    class Builder(val context: Activity) {
        private var view: View? = null
        private var items: MutableList<Menu> = mutableListOf()
        private var callBack: ContextDialogCallBack? = null
        private var customViewResId: Int? = null
        private var customData: CustomData? = null
        private var cornerRadius: Float? = null
        private var fillWidth: Boolean? = null
        private var height: Int? = null
        private var width: Int? = null
        private var blur: Float? = null
        private var backgroundColor: Int? = null

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

        fun setBackgroundColor(backgroundColor: Int?): Builder {
            this.backgroundColor = backgroundColor
            return this
        }

        fun getBackgroundColor(): Int? {
            return backgroundColor
        }

        fun setCustomData(customData: CustomData?): Builder {
            this.customData = customData
            return this
        }

        fun getCustomData(): CustomData? {
            return customData
        }

        fun setCornerRadius(cornerRadius: Float?): Builder {
            this.cornerRadius = cornerRadius
            return this
        }

        fun getCornerRadius(): Float? {
            return cornerRadius
        }

        fun setFillWidth(fillWidth: Boolean?): Builder {
            this.fillWidth = fillWidth
            return this
        }

        fun getFillWidth(): Boolean? {
            return fillWidth
        }

        fun setHeight(height: Int?): Builder {
            this.height = height
            return this
        }

        fun getHeight(): Int? {
            return height
        }

        fun setWidth(width: Int?): Builder {
            this.width = width
            return this
        }

        fun getWidth(): Int? {
            return width
        }

        fun setBlur(blur: Float?): Builder {
            this.blur = blur
            return this
        }

        fun getBlur(): Float? {
            return blur
        }

        fun build(): ContextDialog {
            return ContextDialog(this)
        }
    }

}
