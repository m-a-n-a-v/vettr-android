package com.vettr.android.feature.portfolio

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vettr.android.core.data.repository.PortfolioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject

data class CsvHolding(
    val ticker: String,
    val quantity: Double,
    val avgCost: Double
)

data class CreatePortfolioUiState(
    val name: String = "",
    val provider: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val csvHoldings: List<CsvHolding> = emptyList(),
    val csvErrors: List<String> = emptyList(),
    val createdPortfolioId: String? = null
)

@HiltViewModel
class CreatePortfolioViewModel @Inject constructor(
    private val portfolioRepository: PortfolioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePortfolioUiState())
    val uiState: StateFlow<CreatePortfolioUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name, error = null)
    }

    fun selectProvider(provider: String) {
        _uiState.value = _uiState.value.copy(provider = provider, error = null)
    }

    fun parseCsv(inputStream: InputStream) {
        viewModelScope.launch {
            try {
                val holdings = mutableListOf<CsvHolding>()
                val errors = mutableListOf<String>()
                val reader = BufferedReader(InputStreamReader(inputStream))

                var lineNumber = 0
                var headerSkipped = false

                reader.forEachLine { line ->
                    lineNumber++
                    if (!headerSkipped) {
                        headerSkipped = true
                        // Check if first line is header
                        val lower = line.lowercase()
                        if (lower.contains("ticker") || lower.contains("symbol") || lower.contains("stock")) {
                            return@forEachLine
                        }
                    }

                    try {
                        val fields = parseCsvLine(line)
                        if (fields.size >= 3) {
                            val ticker = fields[0].trim().uppercase()
                            val quantity = fields[1].trim().toDoubleOrNull()
                            val avgCost = fields[2].trim().toDoubleOrNull()

                            if (ticker.isNotEmpty() && quantity != null && avgCost != null && quantity > 0 && avgCost > 0) {
                                holdings.add(CsvHolding(ticker, quantity, avgCost))
                            } else {
                                errors.add("Row $lineNumber: Invalid data (ticker=$ticker, qty=${fields[1]}, cost=${fields[2]})")
                            }
                        } else if (line.isNotBlank()) {
                            errors.add("Row $lineNumber: Expected at least 3 columns (ticker, quantity, avg cost)")
                        }
                    } catch (e: Exception) {
                        errors.add("Row $lineNumber: ${e.message}")
                    }
                }

                _uiState.value = _uiState.value.copy(
                    csvHoldings = holdings,
                    csvErrors = errors
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse CSV")
                _uiState.value = _uiState.value.copy(error = "Failed to read CSV file: ${e.message}")
            }
        }
    }

    fun createPortfolio() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.value = state.copy(error = "Portfolio name is required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = portfolioRepository.createPortfolio(
                name = state.name,
                provider = state.provider.ifEmpty { "manual" }
            )

            result.onSuccess { portfolio ->
                val portfolioId = portfolio.id

                // If CSV holdings exist, add them
                if (state.csvHoldings.isNotEmpty()) {
                    state.csvHoldings.forEach { holding ->
                        portfolioRepository.addHolding(
                            portfolioId = portfolioId,
                            ticker = holding.ticker,
                            quantity = holding.quantity,
                            avgCost = holding.avgCost
                        )
                    }
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    createdPortfolioId = portfolioId
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to create portfolio"
                )
            }
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    fields.add(current.toString())
                    current.clear()
                }
                else -> current.append(char)
            }
        }
        fields.add(current.toString())
        return fields
    }
}
