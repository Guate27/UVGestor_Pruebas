package com.uvg.uvgestor.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.uvg.uvgestor.ui.data.FinancialAdvice
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAdviceDataSource {

    private val firestore = FirebaseFirestore.getInstance()
    private val adviceCollection = firestore.collection("financial_advice")

    suspend fun addAdvice(advice: FinancialAdvice): Result<String> {
        return try {
            val adviceMap = hashMapOf(
                "text" to advice.text,
                "category" to advice.category,
                "createdAt" to advice.createdAt
            )

            val docRef = adviceCollection.add(adviceMap).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllAdvice(): Result<List<FinancialAdvice>> {
        return try {
            val snapshot = adviceCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val adviceList = snapshot.documents.mapNotNull { doc ->
                try {
                    FinancialAdvice(
                        id = doc.id,
                        text = doc.getString("text") ?: "",
                        category = doc.getString("category") ?: "",
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(adviceList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAdviceByCategory(category: String): Result<List<FinancialAdvice>> {
        return try {
            val snapshot = adviceCollection
                .whereEqualTo("category", category)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val adviceList = snapshot.documents.mapNotNull { doc ->
                try {
                    FinancialAdvice(
                        id = doc.id,
                        text = doc.getString("text") ?: "",
                        category = doc.getString("category") ?: "",
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(adviceList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAdviceFlow(): Flow<List<FinancialAdvice>> = callbackFlow {
        val listener = adviceCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val adviceList = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        FinancialAdvice(
                            id = doc.id,
                            text = doc.getString("text") ?: "",
                            category = doc.getString("category") ?: "",
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(adviceList)
            }

        awaitClose { listener.remove() }
    }

    suspend fun initializeDefaultAdvice(): Result<Unit> {
        return try {
            // Verificar si ya existen consejos
            val snapshot = adviceCollection.limit(1).get().await()
            if (!snapshot.isEmpty) {
                return Result.success(Unit)
            }

            // Lista de consejos predeterminados
            val defaultAdvice = listOf(
                // Gastos
                FinancialAdvice(
                    text = "Lleva un registro diario de tus gastos, aunque sean pequeños. Los Q5 del café diario suman Q150 al mes.",
                    category = "Gastos"
                ),
                FinancialAdvice(
                    text = "Antes de comprar algo, espera 24 horas. Si aún lo necesitas después, entonces cómpralo.",
                    category = "Gastos"
                ),
                FinancialAdvice(
                    text = "Usa la regla 50/30/20: 50% necesidades, 30% gustos, 20% ahorro.",
                    category = "Gastos"
                ),
                FinancialAdvice(
                    text = "Evita las compras impulsivas. Haz una lista antes de ir al supermercado.",
                    category = "Gastos"
                ),
                FinancialAdvice(
                    text = "Cocina en casa en lugar de comer fuera. Puedes ahorrar hasta 70% en alimentación.",
                    category = "Gastos"
                ),

                // Ahorro
                FinancialAdvice(
                    text = "Ahorra primero, gasta después. Apenas recibas dinero, separa un porcentaje para ahorros.",
                    category = "Ahorro"
                ),
                FinancialAdvice(
                    text = "Crea un fondo de emergencia equivalente a 3 meses de tus gastos básicos.",
                    category = "Ahorro"
                ),
                FinancialAdvice(
                    text = "Usa el método del desafío de las 52 semanas: ahorra Q5 la primera semana, Q10 la segunda, y así sucesivamente.",
                    category = "Ahorro"
                ),
                FinancialAdvice(
                    text = "Abre una cuenta de ahorro separada para no mezclar tu dinero de gastos con tus ahorros.",
                    category = "Ahorro"
                ),
                FinancialAdvice(
                    text = "Ahorra el cambio. Redondea tus gastos y guarda la diferencia.",
                    category = "Ahorro"
                ),

                // Inversión
                FinancialAdvice(
                    text = "Educa tu mente financiera. Lee al menos un libro de finanzas personales al semestre.",
                    category = "Inversión"
                ),
                FinancialAdvice(
                    text = "Invierte en ti mismo: cursos, certificaciones y habilidades te darán mejores ingresos futuros.",
                    category = "Inversión"
                ),
                FinancialAdvice(
                    text = "Considera inversiones pequeñas en fondos indexados una vez tengas tu fondo de emergencia.",
                    category = "Inversión"
                ),
                FinancialAdvice(
                    text = "Aprende sobre interés compuesto. Empezar a invertir joven te da una ventaja enorme.",
                    category = "Inversión"
                ),
                FinancialAdvice(
                    text = "Diversifica tus fuentes de ingreso. Busca trabajos freelance relacionados con tu carrera.",
                    category = "Inversión"
                )
            )

            // Agregar todos los consejos a Firestore
            defaultAdvice.forEach { advice ->
                addAdvice(advice)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}