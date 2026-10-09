package com.example.shelfcount.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.shelfcount.data.ShelfCountPreferences
import com.example.shelfcount.data.shelfCountDataStore

class ShelfCountViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShelfCountViewModel::class.java)) {
            val prefs = ShelfCountPreferences(context.applicationContext.shelfCountDataStore)
            return ShelfCountViewModel(prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
