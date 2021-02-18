# ContextMenu
Show amazing context menu simply
# Preview 
[![Demo CountPages alpha](https://img.youtube.com/vi/BX21a2VNu10/maxresdefault.jpg)](https://youtu.be/BX21a2VNu10)
# dependencies 
Add it in your root build.gradle at the end of repositories:
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
Add the dependency :
```
implementation 'com.github.saadabusham:ContextMenu<latest-version>'
```
# Example 
Setup xml design
```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout android:id="@+id/relativeContainer"
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    xmlns:app="http://schemas.android.com/apk/res-auto">

    <com.google.android.material.appbar.MaterialToolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="55dp"
        app:title="Single View"
        app:navigationIcon="@drawable/ic_arrow_back"/>
    <ImageView
        android:id="@+id/imgSample"
        android:layout_width="match_parent"
        android:layout_height="300dp"
        android:layout_gravity="center"
        android:scaleType="centerCrop"
        android:layout_centerInParent="true"/>

</RelativeLayout>
```
Add view preferences 
``` kotlin 
val relativeContainer : RelativeLayout = findViewById(R.id.relativeContainer) //ViewGroup(view's parent)
val imgSample : ImageView = findViewById(R.id.imgSample) //view to popup
```
Get your menu list ready
``` kotlin 
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
```
Add click listener
``` kotlin
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
```
