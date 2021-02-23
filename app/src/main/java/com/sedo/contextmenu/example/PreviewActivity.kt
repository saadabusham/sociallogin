package com.sedo.contextmenu.example

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.sedo.contextmenu.data.models.Menu
import com.sedo.contextmenu.ui.ContextDialog

class PreviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
        val relativeContainer : RelativeLayout = findViewById(R.id.relativeContainer)
        val imgSample : ImageView = findViewById(R.id.imgSample)
        val toolbar : MaterialToolbar = findViewById(R.id.toolbar)
        imgSample.setImageResource(intent.getIntExtra("imageRes",R.drawable.sample_image))
        imgSample.setOnLongClickListener {
            ContextDialog(this,relativeContainer,it,getMenuItems(),object : ContextDialog.ContextDialogCallBack{
                override fun returned(item: Menu?, position: Int) {
                    item?.let { it ->
                        Toast.makeText(this@PreviewActivity,it.title,Toast.LENGTH_SHORT).show()
                    }
                }
            }).show()
            return@setOnLongClickListener true
        }
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun getMenuItems(): List<Menu> {
        val close = Menu(resources.getString(R.string.menu_close), R.drawable.ic_menu_cancel)
        val removeItem = Menu(resources.getString(R.string.menu_remove_item),
            R.drawable.ic_menu_remove
        )
        val removeAll = Menu(resources.getString(R.string.menu_clear_all),
            R.drawable.ic_menu_delete_all
        )
        return mutableListOf<Menu>().apply {
            add(close)
            add(removeItem)
            add(removeAll)
        }
    }
}