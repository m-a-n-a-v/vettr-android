package com.vettr.android.feature.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vettr.android.core.data.remote.NewsArticleResponse
import com.vettr.android.core.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class NewsUiState(
    val articles: List<NewsArticleResponse> = emptyList(),
    val filteredArticles: List<NewsArticleResponse> = emptyList(),
    val sources: List<String> = emptyList(),
    val selectedSource: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    init {
        loadNews()
    }

    fun loadNews() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = newsRepository.getNews(
                source = _uiState.value.selectedSource,
                limit = 50
            )

            result.onSuccess { articles ->
                val sources = articles.map { it.source }.distinct().sorted()
                _uiState.value = _uiState.value.copy(
                    articles = articles,
                    filteredArticles = articles,
                    sources = sources,
                    isLoading = false
                )
            }.onFailure { e ->
                Timber.e(e, "Failed to load news")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load news"
                )
            }
        }
    }

    fun selectSource(source: String?) {
        _uiState.value = _uiState.value.copy(selectedSource = source)
        if (source == null) {
            _uiState.value = _uiState.value.copy(filteredArticles = _uiState.value.articles)
        } else {
            _uiState.value = _uiState.value.copy(
                filteredArticles = _uiState.value.articles.filter { it.source == source }
            )
        }
    }

    fun refresh() {
        loadNews()
    }
}
