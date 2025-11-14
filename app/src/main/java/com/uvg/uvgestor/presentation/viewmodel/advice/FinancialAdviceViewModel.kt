package com.uvg.uvgestor.presentation.viewmodel.advice

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uvg.uvgestor.data.repository.FinancialAdviceRepository
import com.uvg.uvgestor.domain.model.NetworkResult
import com.uvg.uvgestor.ui.data.FinancialAdvice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FinancialAdviceUiState(
    val adviceList: List<FinancialAdvice> = emptyList(),
    val filteredAdviceList: List<FinancialAdvice> = emptyList(),
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isInitialized: Boolean = false
)

sealed class FinancialAdviceUiEvent {
    data class SearchQueryChanged(val query: String) : FinancialAdviceUiEvent()
    data class CategorySelected(val category: String?) : FinancialAdviceUiEvent()
    object LoadAdvice : FinancialAdviceUiEvent()
    object InitializeDefaultAdvice : FinancialAdviceUiEvent()
    object ErrorDismissed : FinancialAdviceUiEvent()
    object RetryLoad : FinancialAdviceUiEvent()
}

class FinancialAdviceViewModel(application: Application) : AndroidViewModel(application) {

    private val adviceRepository = FinancialAdviceRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(FinancialAdviceUiState())
    val uiState: StateFlow<FinancialAdviceUiState> = _uiState.asStateFlow()

    init {
        loadAdvice()
    }

    fun onEvent(event: FinancialAdviceUiEvent) {
        when (event) {
            is FinancialAdviceUiEvent.SearchQueryChanged -> {
                _uiState.value = _uiState.value.copy(searchQuery = event.query)
                performSearch(event.query)
            }
            is FinancialAdviceUiEvent.CategorySelected -> {
                _uiState.value = _uiState.value.copy(selectedCategory = event.category)
                filterByCategory(event.category)
            }
            FinancialAdviceUiEvent.LoadAdvice -> {
                loadAdvice()
            }
            FinancialAdviceUiEvent.InitializeDefaultAdvice -> {
                initializeDefaultAdvice()
            }
            FinancialAdviceUiEvent.ErrorDismissed -> {
                _uiState.value = _uiState.value.copy(error = null)
            }
            FinancialAdviceUiEvent.RetryLoad -> {
                loadAdvice()
            }
        }
    }

    private fun loadAdvice() {
        viewModelScope.launch {
            adviceRepository.getAllAdvice().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            error = null
                        )
                    }
                    is NetworkResult.Success -> {
                        val advice = result.data
                        _uiState.value = _uiState.value.copy(
                            adviceList = advice,
                            filteredAdviceList = advice,
                            isLoading = false,
                            error = null,
                            isInitialized = advice.isNotEmpty()
                        )

                        // Si no hay consejos, inicializar con los predeterminados
                        if (advice.isEmpty()) {
                            initializeDefaultAdvice()
                        }
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    private fun initializeDefaultAdvice() {
        viewModelScope.launch {
            adviceRepository.initializeDefaultAdvice().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            error = null
                        )
                    }
                    is NetworkResult.Success -> {
                        // Recargar los consejos después de inicializarlos
                        loadAdvice()
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    private fun filterByCategory(category: String?) {
        val currentState = _uiState.value

        if (category == null) {
            // Mostrar todos
            _uiState.value = currentState.copy(
                filteredAdviceList = currentState.adviceList
            )
        } else {
            viewModelScope.launch {
                adviceRepository.getAdviceByCategory(category).collect { result ->
                    when (result) {
                        is NetworkResult.Loading -> {
                            _uiState.value = currentState.copy(isLoading = true)
                        }
                        is NetworkResult.Success -> {
                            _uiState.value = currentState.copy(
                                filteredAdviceList = result.data,
                                isLoading = false
                            )
                        }
                        is NetworkResult.Error -> {
                            _uiState.value = currentState.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    private fun performSearch(query: String) {
        val currentState = _uiState.value

        if (query.isBlank()) {
            // Si no hay búsqueda, aplicar filtro de categoría o mostrar todos
            filterByCategory(currentState.selectedCategory)
            return
        }

        viewModelScope.launch {
            adviceRepository.searchAdvice(query).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _uiState.value = currentState.copy(isLoading = true)
                    }
                    is NetworkResult.Success -> {
                        val filtered = if (currentState.selectedCategory != null) {
                            result.data.filter { it.category == currentState.selectedCategory }
                        } else {
                            result.data
                        }

                        _uiState.value = currentState.copy(
                            filteredAdviceList = filtered,
                            isLoading = false
                        )
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }
}