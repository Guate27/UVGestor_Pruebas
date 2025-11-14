package com.uvg.uvgestor.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.uvg.uvgestor.data.local.dao.ExpenseDao
import com.uvg.uvgestor.data.local.dao.IncomeDao
import com.uvg.uvgestor.data.local.dao.UserDao
import com.uvg.uvgestor.data.local.dao.BudgetDao
import com.uvg.uvgestor.data.local.dao.FinancialAdviceDao
import com.uvg.uvgestor.data.local.entity.ExpenseEntity
import com.uvg.uvgestor.data.local.entity.IncomeEntity
import com.uvg.uvgestor.data.local.entity.UserEntity
import com.uvg.uvgestor.data.local.entity.BudgetEntity
import com.uvg.uvgestor.data.local.entity.FinancialAdviceEntity

@Database(
    entities = [
        ExpenseEntity::class,
        IncomeEntity::class,
        UserEntity::class,
        BudgetEntity::class,
        FinancialAdviceEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeDao(): IncomeDao
    abstract fun userDao(): UserDao
    abstract fun budgetDao(): BudgetDao
    abstract fun financialAdviceDao(): FinancialAdviceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "uvgestor_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}