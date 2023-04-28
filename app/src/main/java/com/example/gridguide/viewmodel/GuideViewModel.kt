package com.example.gridguide.viewmodel

import androidx.lifecycle.ViewModel
import com.example.gridguide.repository.GuideRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GuideViewModel @Inject constructor(
    private val repository: GuideRepository,
): ViewModel() {

}