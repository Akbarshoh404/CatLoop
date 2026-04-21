package uz.angrykitten.catloop.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "catloop_prefs")

class HighScoreRepository(private val context: Context) {

    private val HIGH_SCORE_KEY = intPreferencesKey("high_score")

    /** Emits the current high score (0 if never set). */
    val highScore: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[HIGH_SCORE_KEY] ?: 0
    }

    /** Persists [score] only if it exceeds the stored high score. */
    suspend fun saveHighScore(score: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[HIGH_SCORE_KEY] ?: 0
            if (score > current) {
                prefs[HIGH_SCORE_KEY] = score
            }
        }
    }
}
