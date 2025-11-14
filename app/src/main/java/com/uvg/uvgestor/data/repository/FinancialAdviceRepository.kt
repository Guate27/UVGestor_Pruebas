package com.uvg.uvgestor.data.repository

import android.content.Context
import com.uvg.uvgestor.data.local.database.AppDatabase
import com.uvg.uvgestor.data.local.entity.toEntity
import com.uvg.uvgestor.data.local.entity.toFinancialAdvice
import com.uvg.uvgestor.data.remote.FirebaseAdviceDataSource
import com.uvg.uvgestor.domain.model.NetworkResult
import com.uvg.uvgestor.ui.data.FinancialAdvice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class FinancialAdviceRepository(private val context: Context) {

    private val firebaseDataSource = FirebaseAdviceDataSource()
    private val database = AppDatabase.getInstance(context)
    private val adviceDao = database.financialAdviceDao()

    suspend fun initializeDefaultAdvice(): Flow<NetworkResult<Unit>> = flow {
        emit(NetworkResult.Loading)

        try {
            val result = firebaseDataSource.initializeDefaultAdvice()

            result.fold(
                onSuccess = {
                    emit(NetworkResult.Success(Unit))
                },
                onFailure = { exception ->
                    emit(NetworkResult.Error("Error al inicializar consejos: ${exception.message}"))
                }
            )
        } catch (e: Exception) {
            emit(NetworkResult.Error("Error inesperado: ${e.message}"))
        }
    }

    suspend fun getAllAdvice(): Flow<NetworkResult<List<FinancialAdvice>>> = flow {
        emit(NetworkResult.Loading)

        try {
            // Primero intentar cargar desde cache local
            val localAdvice = adviceDao.getAllAdvice().map { it.toFinancialAdvice() }

            if (localAdvice.isNotEmpty()) {
                emit(NetworkResult.Success(localAdvice))
            }

            // Luego sincronizar con Firebase
            val result = firebaseDataSource.getAllAdvice()

            result.fold(
                onSuccess = { firebaseAdvice ->
                    // Actualizar cache local
                    adviceDao.deleteAll()
                    adviceDao.insertAll(firebaseAdvice.map { it.toEntity() })

                    emit(NetworkResult.Success(firebaseAdvice))
                },
                onFailure = { exception ->
                    if (localAdvice.isEmpty()) {
                        emit(NetworkResult.Error("Error al cargar consejos: ${exception.message}"))
                    }
                }
            )
        } catch (e: Exception) {
            emit(NetworkResult.Error("Error inesperado: ${e.message}"))
        }
    }

    fun getAdviceFlow(): Flow<List<FinancialAdvice>> {
        return adviceDao.getAllAdviceFlow().map { entities ->
            entities.map { it.toFinancialAdvice() }
        }
    }

    suspend fun getAdviceByCategory(category: String): Flow<NetworkResult<List<FinancialAdvice>>> = flow {
        emit(NetworkResult.Loading)

        try {
            val localAdvice = adviceDao.getAdviceByCategory(category).map { it.toFinancialAdvice() }

            if (localAdvice.isNotEmpty()) {
                emit(NetworkResult.Success(localAdvice))
            }

            val result = firebaseDataSource.getAdviceByCategory(category)

            result.fold(
                onSuccess = { firebaseAdvice ->
                    emit(NetworkResult.Success(firebaseAdvice))
                },
                onFailure = { exception ->
                    if (localAdvice.isEmpty()) {
                        emit(NetworkResult.Error("Error al cargar consejos: ${exception.message}"))
                    }
                }
            )
        } catch (e: Exception) {
            emit(NetworkResult.Error("Error inesperado: ${e.message}"))
        }
    }

    suspend fun searchAdvice(query: String): Flow<NetworkResult<List<FinancialAdvice>>> = flow {
        emit(NetworkResult.Loading)

        try {
            val searchResults = adviceDao.searchAdvice(query).map { it.toFinancialAdvice() }
            emit(NetworkResult.Success(searchResults))
        } catch (e: Exception) {
            emit(NetworkResult.Error("Error en búsqueda: ${e.message}"))
        }
    }
}