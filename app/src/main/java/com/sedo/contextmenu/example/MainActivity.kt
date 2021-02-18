package com.sedo.contextmenu.example

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.sedo.contextmenu.data.models.Menu
import com.sedo.contextmenu.ui.ContextDialog

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val relativeContainer : RelativeLayout = findViewById(R.id.relativeContainer)
        val imgClose : ImageView = findViewById(R.id.imgClose)
        val imgEdit : ImageView = findViewById(R.id.imgEdit)
        val imgRemove : ImageView = findViewById(R.id.imgRemove)
        imgClose.setOnClickListener {
            ContextDialog(this,relativeContainer,it,getMenuItems(),object : ContextDialog.ContextDialogCallBack{
                override fun returned(item: Menu?, position: Int) {
                    item?.let { it ->
                        Toast.makeText(this@MainActivity,it.title,Toast.LENGTH_SHORT).show()
                    }
                }
            }).show()
        }
        imgEdit.setOnClickListener {
            ContextDialog(this,relativeContainer,it,getMenuItems(),object : ContextDialog.ContextDialogCallBack{
                override fun returned(item: Menu?, position: Int) {
                    item?.let { it ->
                        Toast.makeText(this@MainActivity,it.title,Toast.LENGTH_SHORT).show()
                    }
                }
            }).show()
        }
        imgRemove.setOnClickListener {
            ContextDialog(this,relativeContainer,it,getMenuItems(),object : ContextDialog.ContextDialogCallBack{
                override fun returned(item: Menu?, position: Int) {
                    item?.let { it ->
                        Toast.makeText(this@MainActivity,it.title,Toast.LENGTH_SHORT).show()
                    }
                }
            }).show()
        }
    }

    private fun getMenuItems(): List<Menu> {
        val close = Menu(resources.getString(R.string.menu_close), R.drawable.ic_menu_cancel)
        val edit = Menu(resources.getString(R.string.menu_edit), R.drawable.ic_menu_edit)
        val removeItem = Menu(resources.getString(R.string.menu_remove_item),
            R.drawable.ic_menu_remove
        )
        val removeAll = Menu(resources.getString(R.string.menu_clear_all),
            R.drawable.ic_menu_delete_all
        )
        return mutableListOf<Menu>().apply {
            add(close)
            add(edit)
            add(removeItem)
            add(removeAll)
        }
    }
}