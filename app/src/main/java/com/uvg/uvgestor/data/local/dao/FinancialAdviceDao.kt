package com.uvg.uvgestor.data.local.dao

import androidx.room.*
import com.uvg.uvgestor.data.local.entity.FinancialAdviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialAdviceDao {

    @Query("SELECT * FROM financial_advice ORDER BY createdAt DESC")
    fun getAllAdviceFlow(): Flow<List<FinancialAdviceEntity>>

    @Query("SELECT * FROM financial_advice ORDER BY createdAt DESC")
    suspend fun getAllAdvice(): List<FinancialAdviceEntity>

    @Query("SELECT * FROM financial_advice WHERE category = :category ORDER BY createdAt DESC")
    suspend fun getAdviceByCategory(category: String): List<FinancialAdviceEntity>

    @Query("SELECT * FROM financial_advice WHERE text LIKE '%' || :searchQuery || '%' ORDER BY createdAt DESC")
    suspend fun searchAdvice(searchQuery: String): List<FinancialAdviceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvice(advice: FinancialAdviceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(adviceList: List<FinancialAdviceEntity>)

    @Query("DELETE FROM financial_advice")
    suspend fun deleteAll()
}