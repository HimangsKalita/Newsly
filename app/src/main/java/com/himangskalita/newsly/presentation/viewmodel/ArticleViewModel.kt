package com.himangskalita.newsly.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ArticleViewModel @Inject constructor(): ViewModel() {

    private val _stateChanged = MutableLiveData(false)
    val stateChanged: LiveData<Boolean>
        get() = _stateChanged

    fun changeStateChanged() {

        _stateChanged.postValue(true)
    }
}