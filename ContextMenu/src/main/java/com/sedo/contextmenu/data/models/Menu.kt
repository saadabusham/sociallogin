package com.sedo.contextmenu.data.models

data class Menu(
    val title: String,
    val icon: Int,
    val titleColor: Int? = null,
    val titleColorHex: String? = null,
    val iconTint: Int? = null,
    val iconTintHex: String? = null
)
