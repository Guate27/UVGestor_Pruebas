package com.uvg.uvgestor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.uvg.uvgestor.ui.data.FinancialAdvice

@Entity(tableName = "financial_advice")
data class FinancialAdviceEntity(
    @PrimaryKey
    val id: String,
    val text: String,
    val category: String,
    val createdAt: Long = System.currentTimeMillis()
)

fun FinancialAdviceEntity.toFinancialAdvice(): FinancialAdvice {
    return FinancialAdvice(
        id = id,
        text = text,
        category = category,
        createdAt = createdAt
    )
}

fun FinancialAdvice.toEntity(): FinancialAdviceEntity {
    return FinancialAdviceEntity(
        id = id,
        text = text,
        category = category,
        createdAt = createdAt
    )
}