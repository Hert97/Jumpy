package com.example.Activity

import android.util.Log
import androidx.lifecycle.*
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

    private val allScore: LiveData<List<Score>> = repository.allScore.asLiveData()


    fun deleteScore(score: Score) = viewModelScope.launch {
        repository.deleteScore(score)
    }
    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }

    fun getAllScore() = allScore

    fun insertScore(score: Score) = viewModelScope.launch {
        repository.insertScore(score)
        Log.d("Insert_Score",score.value.toString())
    }
}