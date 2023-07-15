package jp.kawagh.kiando

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.kawagh.kiando.data.PreferencesRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingUiState(val reverseBoardSigns: Boolean)

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {
    var uiState by mutableStateOf(SettingUiState(false))
        private set

    init {
        viewModelScope.launch {
            preferencesRepository.reverseBoardSigns.collect {
                uiState = uiState.copy(reverseBoardSigns = it)
            }
        }
    }

    fun toggleReverseBoardSigns() {
        val newValue = !uiState.reverseBoardSigns
        viewModelScope.launch {
            preferencesRepository.setReverseBoardSigns(newValue)
        }
    }
}
