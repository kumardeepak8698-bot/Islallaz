package com.example.model

data class AppInfo(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val isSystem: Boolean,
    val isFrozen: Boolean,
    val isCloned: Boolean,
    val customLabel: String? = null,
    val category: String = "General",
    val notes: String = "",
    val isAutoFreeze: Boolean = false
)
