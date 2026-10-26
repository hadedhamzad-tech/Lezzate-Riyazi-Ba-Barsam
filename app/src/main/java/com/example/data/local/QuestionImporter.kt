package com.example.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.data.settings.settingsDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.security.MessageDigest

class QuestionImporter(
    private val context: Context,
    private val questionDao: QuestionDao,
    private val dataStore: DataStore<Preferences> = context.settingsDataStore
) {
    companion object {
        const val DEFAULT_ASSET_PATH = "questions/bank_v1.json"
        val KEY_QUESTION_BANK_HASH = stringPreferencesKey("question_bank_sha256")
        private const val TAG = "QuestionImporter"
        private const val BATCH_SIZE = 200

        fun computeSha256(bytes: ByteArray): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(bytes)
            return hashBytes.joinToString("") { "%02x".format(it) }
        }

        fun parseQuestions(bytes: ByteArray): List<QuestionEntity> {
            val jsonString = String(bytes, Charsets.UTF_8)
            val jsonArray = JSONArray(jsonString)
            val questions = ArrayList<QuestionEntity>(jsonArray.length())
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                questions.add(
                    QuestionEntity(
                        id = obj.getString("id"),
                        grade = obj.getInt("grade"),
                        difficulty = obj.getString("difficulty"),
                        topic = obj.getString("topic"),
                        questionText = obj.getString("questionText"),
                        option1 = obj.getString("option1"),
                        option2 = obj.getString("option2"),
                        option3 = obj.getString("option3"),
                        option4 = obj.getString("option4"),
                        correctOptionIndex = obj.getInt("correctOptionIndex"),
                        explanation = obj.getString("explanation")
                    )
                )
            }
            return questions
        }
    }

    suspend fun importFromAsset(assetPath: String = DEFAULT_ASSET_PATH): Boolean = withContext(Dispatchers.IO) {
        try {
            val bytes = context.assets.open(assetPath).use { it.readBytes() }
            importBytes(bytes)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import questions from asset: $assetPath", e)
            false
        }
    }

    suspend fun importBytes(bytes: ByteArray): Boolean = withContext(Dispatchers.IO) {
        try {
            val newHash = computeSha256(bytes)
            val currentHash = dataStore.data.first()[KEY_QUESTION_BANK_HASH]
            if (currentHash == newHash) {
                Log.d(TAG, "Question bank already up to date (hash matches).")
                return@withContext true
            }

            val questions = parseQuestions(bytes)
            questions.chunked(BATCH_SIZE).forEach { batch ->
                questionDao.insertQuestions(batch)
            }

            dataStore.edit { preferences ->
                preferences[KEY_QUESTION_BANK_HASH] = newHash
            }
            Log.d(TAG, "Imported ${questions.size} questions successfully.")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse/import question bytes", e)
            false
        }
    }
}
