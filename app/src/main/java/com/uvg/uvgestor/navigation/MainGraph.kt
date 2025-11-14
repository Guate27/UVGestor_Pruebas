package com.uvg.uvgestor.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.uvg.uvgestor.ui.screens.AddExpenseScreen
import com.uvg.uvgestor.ui.screens.AddIncomeScreen
import com.uvg.uvgestor.ui.screens.BudgetScreen
import com.uvg.uvgestor.ui.screens.HomeScreen
import com.uvg.uvgestor.ui.screens.TransactionDetailScreen
import com.uvg.uvgestor.ui.screens.TransactionsScreen
import com.uvg.uvgestor.ui.screens.FinancialAdviceScreen

fun NavGraphBuilder.mainGraph(navController: NavHostController) {
    navigation(
        startDestination = Screen.Home.route,
        route = Screen.MAIN_GRAPH_ROUTE
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.AddExpense.route) {
            AddExpenseScreen(navController = navController)
        }

        composable(Screen.AddIncome.route) {
            AddIncomeScreen(navController = navController)
        }

        composable(Screen.Budget.route) {
            BudgetScreen(navController = navController)
        }

        composable(Screen.Transactions.route) {
            TransactionsScreen(navController = navController)
        }

        composable(Screen.FinancialAdvice.route) {
            FinancialAdviceScreen(navController = navController)
        }

        composable(
            route = Screen.TransactionDetail.route,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            TransactionDetailScreen(id = id, navController = navController)
        }
    }
}