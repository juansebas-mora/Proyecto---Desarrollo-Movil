package com.example.urumbox.mapasactivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BusquedaViewModel : ViewModel() {
    private val _query = MutableLiveData<String>("")
    val query: LiveData<String> = _query

    fun setQuery(newQuery: String) {
        _query.value = newQuery
    }
}
