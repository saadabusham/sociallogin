package com.sedo.contextmenu.example

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.sedo.contextmenu.data.models.Menu
import com.sedo.contextmenu.ui.ContextDialog
import com.sedo.contextmenu.ui.base.BaseBindingRecyclerViewAdapter
import com.sedo.contextmenu.utils.extensions.setOnItemClickListener

class RecyclerActivity : AppCompatActivity() {

    lateinit var sampleRecyclerAdapter: SampleRecyclerAdapter
    lateinit var recyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycleer)
        recyclerView = findViewById(R.id.recyclerView)
        setUpAdapter()
    }

    private fun setUpAdapter() {
        sampleRecyclerAdapter = SampleRecyclerAdapter(this)
        recyclerView.adapter = sampleRecyclerAdapter
        recyclerView.setOnItemClickListener(object : BaseBindingRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, position: Int, item: Any) {

            }

            override fun onItemLongClick(view: View?, recyclePosition: Int, image: Any) {
                view?.let {
                    ContextDialog(this@RecyclerActivity, recyclerView, it, getMenuItems(), object : ContextDialog.ContextDialogCallBack {
                        override fun returned(item: Menu?, position: Int) {
                            when (position) {
                                1 -> {
                                    startActivity(Intent(this@RecyclerActivity, PreviewActivity::class.java)
                                            .putExtra("imageRes", image as Int))
                                }
                                2 -> {
                                    sampleRecyclerAdapter.removeAt(recyclePosition)
                                }
                                3 -> {
                                    sampleRecyclerAdapter.clear()
                                }
                                4 -> {
                                    fillSampleData()
                                }
                            }
                        }
                    }).show()
                }
            }
        })
        fillSampleData()
    }

    private fun fillSampleData() {
        sampleRecyclerAdapter.clear()
        sampleRecyclerAdapter.submitItem(R.drawable.sample_image)
        sampleRecyclerAdapter.submitItem(R.drawable.sample_image2)
        sampleRecyclerAdapter.submitItem(R.drawable.sample_image3)
        sampleRecyclerAdapter.submitItem(R.drawable.sample_image4)
        sampleRecyclerAdapter.submitItem(R.drawable.sample_image5)
        sampleRecyclerAdapter.submitItem(R.drawable.sample_image6)
    }

    private fun getMenuItems(): List<Menu> {
        val close = Menu(resources.getString(R.string.menu_close), R.drawable.ic_menu_cancel)
        val edit = Menu(resources.getString(R.string.menu_view), R.drawable.ic_menu_preview)
        val removeItem = Menu(resources.getString(R.string.menu_remove_item),
                R.drawable.ic_menu_remove
        )
        val removeAll = Menu(resources.getString(R.string.menu_clear_all),
                R.drawable.ic_menu_delete_all
        )
        val reload = Menu(resources.getString(R.string.menu_reload),
                R.drawable.ic_menu_reload
        )
        return mutableListOf<Menu>().apply {
            add(close)
            add(edit)
            add(removeItem)
            add(removeAll)
            add(reload)
        }
    }
}