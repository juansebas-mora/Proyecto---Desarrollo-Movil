package com.example.urumbox

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

sealed class AccessMainEvent {
    object ShowAddVisitorDialog : AccessMainEvent()
    object NavigateToRequest : AccessMainEvent()
    object NavigateToConsult : AccessMainEvent()
}

class AccessMainViewModel : ViewModel() {

    private val _uiEvent = MutableLiveData<AccessMainEvent?>()
    val uiEvent: LiveData<AccessMainEvent?> = _uiEvent

    fun onAddVisitorClicked() {
        _uiEvent.value = AccessMainEvent.ShowAddVisitorDialog
    }

    fun onRegisterRequestClicked() {
        _uiEvent.value = AccessMainEvent.NavigateToRequest
    }

    fun onConsultRequestsClicked() {
        _uiEvent.value = AccessMainEvent.NavigateToConsult
    }

    fun onEventConsumed() {
        _uiEvent.value = null
    }
}
