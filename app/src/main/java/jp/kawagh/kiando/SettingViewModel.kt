package jp.kawagh.kiando

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class SettingUiState(val reverseBoardSigns: Boolean)

@HiltViewModel
class SettingViewModel @Inject constructor() : ViewModel() {
    var uiState by mutableStateOf(SettingUiState(false))
        private set

    fun toggleReverseBoardSigns() {
        uiState = uiState.copy(reverseBoardSigns = !uiState.reverseBoardSigns)
    }
}