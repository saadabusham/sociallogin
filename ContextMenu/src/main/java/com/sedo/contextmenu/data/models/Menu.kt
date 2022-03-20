package com.sedo.contextmenu.data.models

data class Menu(
    val title: String,
    val titleColor: Int? = null,
    val titleColorHex: String? = null,
    val icon: Int,
    val iconTint: Int? = null,
    val iconTintHex: String? = null
)
