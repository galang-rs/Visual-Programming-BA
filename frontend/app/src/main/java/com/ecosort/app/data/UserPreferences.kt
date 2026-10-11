package com.ecosort.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.ecosort.app.model.AuthResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ecosort_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val TOKEN_KEY = stringPreferencesKey("auth_token")
        val USER_ID_KEY = longPreferencesKey("user_id")
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val USER_PHONE_KEY = stringPreferencesKey("user_phone")
        val USER_ROLE_KEY = stringPreferencesKey("user_role")
        val USER_BALANCE_KEY = doublePreferencesKey("user_balance")
    }

    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    val userRole: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ROLE_KEY]
    }

    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }

    val userBalance: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[USER_BALANCE_KEY] ?: 0.0
    }

    suspend fun saveAuth(auth: AuthResponse) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = auth.token
            preferences[USER_ID_KEY] = auth.id
            preferences[USER_NAME_KEY] = auth.fullName
            preferences[USER_PHONE_KEY] = auth.phoneNumber
            preferences[USER_ROLE_KEY] = auth.role
            preferences[USER_BALANCE_KEY] = auth.balance
        }
    }

    suspend fun updateBalance(newBalance: Double) {
        context.dataStore.edit { preferences ->
            preferences[USER_BALANCE_KEY] = newBalance
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
