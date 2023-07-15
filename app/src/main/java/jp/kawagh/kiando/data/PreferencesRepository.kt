package jp.kawagh.kiando.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface PreferencesRepository {
    val filter: Flow<String>
    val reverseBoardSigns: Flow<Boolean>
    suspend fun setFilter(value: String)
    suspend fun setReverseBoardSigns(value: Boolean)
}

class ImplPreferencesRepository @Inject constructor(
    private val preferences: DataStore<Preferences>,
) : PreferencesRepository {
    private val filterKey = stringPreferencesKey("filter_key")
    private val reverseBoardSignsKey = booleanPreferencesKey("reverse_board_signs_key")
    override val filter: Flow<String> = preferences.data.map { p -> p[filterKey] ?: "" }
    override val reverseBoardSigns: Flow<Boolean> =
        preferences.data.map { p -> p[reverseBoardSignsKey] ?: false }

    override suspend fun setFilter(value: String) {
        preferences.edit { p ->
            p[filterKey] = value
        }
    }

    override suspend fun setReverseBoardSigns(value: Boolean) {
        preferences.edit { p ->
            p[reverseBoardSignsKey] = value
        }
    }
}
