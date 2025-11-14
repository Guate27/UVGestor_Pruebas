package com.uvg.uvgestor.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
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
import com.uvg.uvgestor.presentation.viewmodel.advice.FinancialAdviceUiEvent
import com.uvg.uvgestor.presentation.viewmodel.advice.FinancialAdviceViewModel
import com.uvg.uvgestor.ui.data.FinancialAdvice

@Composable
fun FinancialAdviceScreen(
    navController: NavHostController,
    viewModel: FinancialAdviceViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FinancialAdviceContent(
        adviceList = uiState.filteredAdviceList,
        selectedCategory = uiState.selectedCategory,
        searchQuery = uiState.searchQuery,
        isLoading = uiState.isLoading,
        error = uiState.error,
        onSearchQueryChange = { viewModel.onEvent(FinancialAdviceUiEvent.SearchQueryChanged(it)) },
        onCategorySelected = { viewModel.onEvent(FinancialAdviceUiEvent.CategorySelected(it)) },
        onErrorDismiss = { viewModel.onEvent(FinancialAdviceUiEvent.ErrorDismissed) },
        onRetryClick = { viewModel.onEvent(FinancialAdviceUiEvent.RetryLoad) },
        onBackClick = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialAdviceContent(
    adviceList: List<FinancialAdvice>,
    selectedCategory: String?,
    searchQuery: String,
    isLoading: Boolean,
    error: String?,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onErrorDismiss: () -> Unit,
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val backgroundColor = Color(0xFFF8FCF8)
    val primaryGreen = Color(0xFF00C853)
    val lightGreen = Color(0xFFE6F4E6)
    val darkText = Color(0xFF0D1C0D)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Consejos Financieros",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
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
                modifier = Modifier.fillMaxSize()
            ) {
                // Barra de búsqueda
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    placeholder = {
                        Text(
                            "¿Qué tipo de consejo buscas?",
                            color = Color(0xFF666666)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = primaryGreen
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = primaryGreen,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        cursorColor = primaryGreen
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Filtros de categoría
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryFilterChip(
                        label = "Todos",
                        selected = selectedCategory == null,
                        onClick = { onCategorySelected(null) },
                        primaryColor = primaryGreen
                    )
                    CategoryFilterChip(
                        label = "Gastos",
                        selected = selectedCategory == "Gastos",
                        onClick = { onCategorySelected("Gastos") },
                        primaryColor = primaryGreen
                    )
                    CategoryFilterChip(
                        label = "Ahorro",
                        selected = selectedCategory == "Ahorro",
                        onClick = { onCategorySelected("Ahorro") },
                        primaryColor = primaryGreen
                    )
                    CategoryFilterChip(
                        label = "Inversión",
                        selected = selectedCategory == "Inversión",
                        onClick = { onCategorySelected("Inversión") },
                        primaryColor = primaryGreen
                    )
                }

                Divider(color = Color(0xFFE0E0E0), modifier = Modifier.padding(vertical = 8.dp))

                // Contenido principal
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = primaryGreen)
                            Text(
                                "Cargando consejos...",
                                color = Color.Gray
                            )
                        }
                    }
                } else if (adviceList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                "🔍",
                                fontSize = 64.sp
                            )
                            Text(
                                "No se encontraron consejos",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = darkText
                            )
                            Text(
                                "Intenta con otra búsqueda o categoría",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header informativo
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = lightGreen
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        "💡 ¿Por qué es importante tu salud financiera?",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = darkText
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Una buena administración financiera universitaria es crucial porque reduce el estrés, te permite concentrarte en tus estudios y evita deudas peligrosas.",
                                        fontSize = 14.sp,
                                        color = Color(0xFF333333),
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }

                        // Lista de consejos
                        items(adviceList) { advice ->
                            AdviceCard(
                                advice = advice,
                                primaryColor = primaryGreen
                            )
                        }

                        // Espaciado final
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            // Error Snackbar
            error?.let {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            it,
                            color = Color(0xFFD32F2F),
                            fontSize = 14.sp
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
fun CategoryFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    primaryColor: Color
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = primaryColor,
            selectedLabelColor = Color.White,
            containerColor = Color.White,
            labelColor = Color(0xFF666666)
        ),
        border = if (selected) {
            null
        } else {
            FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = false,
                borderColor = Color(0xFFE0E0E0)
            )
        }
    )
}

@Composable
fun AdviceCard(
    advice: FinancialAdvice,
    primaryColor: Color
) {
    val categoryColor = when (advice.category) {
        "Gastos" -> Color(0xFFFF6B6B)
        "Ahorro" -> Color(0xFF4ECDC4)
        "Inversión" -> Color(0xFFFFD93D)
        else -> Color.Gray
    }

    val categoryEmoji = when (advice.category) {
        "Gastos" -> "💸"
        "Ahorro" -> "🐷"
        "Inversión" -> "📈"
        else -> "💡"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    categoryEmoji,
                    fontSize = 24.sp
                )

                Surface(
                    color = categoryColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        advice.category,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                advice.text,
                fontSize = 14.sp,
                color = Color(0xFF333333),
                lineHeight = 20.sp
            )
        }
    }
}