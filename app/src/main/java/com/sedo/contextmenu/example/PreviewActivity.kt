package com.sedo.contextmenu.example

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.sedo.contextmenu.data.models.CustomData
import com.sedo.contextmenu.data.models.Menu
import com.sedo.contextmenu.ui.ContextDialog

class PreviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
        val imgSample: ImageView = findViewById(R.id.imgSample)
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        intent.getIntExtra("imageRes", R.drawable.sample_image).let {image->
            imgSample.setImageResource(image)
            imgSample.setOnLongClickListener {
                ContextDialog.Builder(this)
                    .setCustomData(
                        CustomData(
                            image = image
                        )
                    )
                    .setCustomResId(R.layout.layout_custom_view_sample)
                    .setItems(getMenuItems())
                    .setCallBack(object : ContextDialog.ContextDialogCallBack {
                        override fun returned(item: Menu?, position: Int,dialog: ContextDialog) {
                            dialog.dismiss()
                            item?.let { it ->
                                Toast.makeText(this@PreviewActivity, it.title, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }).build().show()
                return@setOnLongClickListener true
            }
        }
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun getMenuItems(): MutableList<Menu> {
        val close = Menu(resources.getString(R.string.menu_close), R.drawable.ic_menu_cancel)
        val removeItem = Menu(
            resources.getString(R.string.menu_remove_item),
            R.drawable.ic_menu_remove
        )
        val removeAll = Menu(
            resources.getString(R.string.menu_clear_all),
            R.drawable.ic_menu_delete_all
        )
        return mutableListOf<Menu>().apply {
            add(close)
            add(removeItem)
            add(removeAll)
        }
    }
}