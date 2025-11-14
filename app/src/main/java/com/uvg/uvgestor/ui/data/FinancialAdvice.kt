package com.uvg.uvgestor.ui.data

data class FinancialAdvice(
    val id: String = "",
    val text: String,
    val category: String, // Gastos, Ahorro, Inversión
    val createdAt: Long = System.currentTimeMillis()
)