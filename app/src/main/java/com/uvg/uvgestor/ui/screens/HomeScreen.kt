package com.uvg.uvgestor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.uvg.uvgestor.navigation.Screen
import com.uvg.uvgestor.presentation.viewmodel.home.HomeUiEvent
import com.uvg.uvgestor.presentation.viewmodel.home.HomeViewModel
import com.uvg.uvgestor.ui.data.Expense

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.showBudgetAlert) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(HomeUiEvent.BudgetAlertDismissed) },
            icon = {
                Text("⚠️", fontSize = 48.sp)
            },
            title = {
                Text(
                    "Alerta de Presupuesto",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(uiState.budgetAlertMessage)
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onEvent(HomeUiEvent.BudgetAlertDismissed) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    )
                ) {
                    Text("Entendido")
                }
            }
        )
    }

    HomeContent(
        selectedPeriod = uiState.selectedPeriod,
        expenses = uiState.expenses,
        isLoading = uiState.isLoading,
        error = uiState.error,
        totalExpenses = uiState.totalExpenses,
        totalIncomes = uiState.totalIncomes,
        balance = uiState.balance,
        expensesByCategory = uiState.expensesByCategory,
        currentBudget = uiState.currentBudget,
        budgetPercentage = uiState.budgetPercentage,
        onPeriodChange = { viewModel.onEvent(HomeUiEvent.PeriodChanged(it)) },
        onRetryClick = { viewModel.onEvent(HomeUiEvent.RetryLoad) },
        onErrorDismiss = { viewModel.onEvent(HomeUiEvent.ErrorDismissed) },
        onAddExpenseClick = { navController.navigate(Screen.AddExpense.route) },
        onAddIncomeClick = { navController.navigate(Screen.AddIncome.route) },
        onBudgetClick = { navController.navigate(Screen.Budget.route) },
        onAdviceClick = { navController.navigate(Screen.FinancialAdvice.route) },
        onLogoutClick = {
            navController.navigate(Screen.AUTH_GRAPH_ROUTE) {
                popUpTo(Screen.MAIN_GRAPH_ROUTE) {
                    inclusive = true
                    saveState = false
                }
                launchSingleTop = true
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    selectedPeriod: String,
    expenses: List<Expense>,
    isLoading: Boolean,
    error: String?,
    totalExpenses: Double,
    totalIncomes: Double,
    balance: Double,
    expensesByCategory: Map<String, Double>,
    currentBudget: com.uvg.uvgestor.ui.data.Budget?,
    budgetPercentage: Int,
    onPeriodChange: (String) -> Unit,
    onRetryClick: () -> Unit,
    onErrorDismiss: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onAddIncomeClick: () -> Unit,
    onBudgetClick: () -> Unit,
    onAdviceClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val uvgGreen = Color(0xFF00C853)
    val backgroundColor = Color(0xFFF5F5F5)
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "UVGestor",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Configuración"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Configurar Presupuesto") },
                            onClick = {
                                showMenu = false
                                onBudgetClick()
                            },
                            leadingIcon = {
                                Text("💰", fontSize = 20.sp)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cerrar Sesión") },
                            onClick = {
                                showMenu = false
                                onLogoutClick()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.ExitToApp,
                                    contentDescription = null,
                                    tint = Color.Red
                                )
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                onAddIncomeClick = onAddIncomeClick,
                onAddExpenseClick = onAddExpenseClick,
                onAdviceClick = onAdviceClick,
                primaryColor = uvgGreen
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // TARJETA DE SALDO GENERAL
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Saldo Total",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Q${String.format("%.2f", balance)}",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Ingresos",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    "Q${String.format("%.2f", totalIncomes)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Gastos",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    "Q${String.format("%.2f", totalExpenses)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // INDICADOR DE PRESUPUESTO
                if (currentBudget != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                budgetPercentage >= 100 -> Color(0xFFFFEBEE)
                                budgetPercentage >= 80 -> Color(0xFFFFF3E0)
                                else -> Color(0xFFE8F5E9)
                            }
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Presupuesto Mensual",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        "Q${String.format("%.0f", totalExpenses)} / Q${String.format("%.0f", currentBudget.limitAmount)}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }

                                Text(
                                    "$budgetPercentage%",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        budgetPercentage >= 100 -> Color(0xFFD32F2F)
                                        budgetPercentage >= 80 -> Color(0xFFFF6F00)
                                        else -> Color(0xFF4CAF50)
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { (budgetPercentage.coerceAtMost(100) / 100f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = when {
                                    budgetPercentage >= 100 -> Color(0xFFD32F2F)
                                    budgetPercentage >= 80 -> Color(0xFFFF6F00)
                                    else -> Color(0xFF4CAF50)
                                },
                                trackColor = Color(0xFFE0E0E0)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                TimePeriodSelector(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = onPeriodChange,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(color = uvgGreen)
                                Text(
                                    "Cargando gastos...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Gastos por Categoría - $selectedPeriod",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (expensesByCategory.isNotEmpty()) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    expensesByCategory.forEach { (category, amount) ->
                                        val percentage = if (totalExpenses > 0) {
                                            (amount / totalExpenses * 100).toInt()
                                        } else 0

                                        CategoryStatItem(
                                            category = category,
                                            amount = amount,
                                            percentage = percentage
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Sin gastos en este período",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Gastos Recientes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (expenses.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No hay gastos registrados",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        expenses.takeLast(5).reversed().forEach { expense ->
                            ExpenseListItem(expense)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }

            error?.let {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            it,
                            color = Color(0xFFD32F2F),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onErrorDismiss) {
                                Text("Cerrar")
                            }
                            TextButton(onClick = onRetryClick) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    onAddIncomeClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onAdviceClick: () -> Unit,
    primaryColor: Color
) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = primaryColor,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = {
                Text(
                    "💰",
                    fontSize = 24.sp
                )
            },
            label = {
                Text(
                    "Ingreso",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            selected = false,
            onClick = onAddIncomeClick,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = primaryColor,
                unselectedIconColor = primaryColor,
                unselectedTextColor = Color.Gray,
                indicatorColor = primaryColor
            )
        )

        NavigationBarItem(
            icon = {
                Text(
                    "💸",
                    fontSize = 24.sp
                )
            },
            label = {
                Text(
                    "Gasto",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            selected = false,
            onClick = onAddExpenseClick,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = primaryColor,
                unselectedIconColor = primaryColor,
                unselectedTextColor = Color.Gray,
                indicatorColor = primaryColor
            )
        )

        NavigationBarItem(
            icon = {
                Text(
                    "💡",
                    fontSize = 24.sp
                )
            },
            label = {
                Text(
                    "Consejos",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            selected = false,
            onClick = onAdviceClick,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = primaryColor,
                unselectedIconColor = primaryColor,
                unselectedTextColor = Color.Gray,
                indicatorColor = primaryColor
            )
        )
    }
}

@Composable
fun TimePeriodSelector(
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    val periods = listOf("Diario", "Semanal", "Mensual", "Anual")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        periods.forEach { period ->
            FilterChip(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                label = {
                    Text(
                        period,
                        fontSize = 14.sp,
                        fontWeight = if (selectedPeriod == period) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.weight(1f),
                enabled = enabled,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF00C853),
                    selectedLabelColor = Color.White,
                    containerColor = Color.White
                )
            )
        }
    }
}

@Composable
fun CategoryStatItem(category: String, amount: Double, percentage: Int) {
    val categoryColor = when (category) {
        "Comida" -> Color(0xFFFF6B6B)
        "Transporte" -> Color(0xFF4ECDC4)
        "Ocio" -> Color(0xFFFFD93D)
        else -> Color.Gray
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(categoryColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                category,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                "Q${String.format("%.2f", amount)}",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
        Text(
            "$percentage%",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = categoryColor
        )
    }
}

@Composable
fun ExpenseListItem(expense: Expense) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    expense.title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${expense.category} • ${expense.timePeriod}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Text(
                "Q${String.format("%.2f", expense.amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )
        }
    }
}