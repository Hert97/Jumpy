package com.example.Activity

import androidx.annotation.WorkerThread
import androidx.lifecycle.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ScoreViewModelFactory(private val repository: ScoreRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScoreViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScoreViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ScoreViewModel(private val repository: ScoreRepo) : ViewModel() {

    private val alldigits: LiveData<List<Score>> = repository.allDigits.asLiveData()


    fun deleteScore(digit: Score) = viewModelScope.launch {
        repository.deleteScore(digit)
    }

    fun getAllScore() = alldigits

    fun insertScore(digit: Score) = viewModelScope.launch {
        repository.insertScore(digit)
    }
}