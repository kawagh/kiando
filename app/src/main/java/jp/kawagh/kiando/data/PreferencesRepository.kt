package jp.kawagh.kiando.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface PreferencesRepository {
    val filter: Flow<String>
    suspend fun setFilter(value: String)
}

class ImplPreferencesRepository @Inject constructor(
    private val preferences: DataStore<Preferences>,
) : PreferencesRepository {
    private val filterKey = stringPreferencesKey("filter_key")
    override val filter: Flow<String> = preferences.data.map { p -> p[filterKey] ?: "" }
    override suspend fun setFilter(value: String) {
        preferences.edit { p ->
            p[filterKey] = value
        }
    }

}