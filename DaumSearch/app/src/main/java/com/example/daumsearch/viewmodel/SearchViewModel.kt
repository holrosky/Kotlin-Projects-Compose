package com.example.daumsearch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.example.daumsearch.model.blog.BlogDocument
import com.example.daumsearch.model.image.ImageDocument
import com.example.daumsearch.model.video.VideoDocument
import com.example.kakaoexam.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _videosFlow = MutableStateFlow<PagingData<VideoDocument>>(PagingData.empty())
    val videosFlow: StateFlow<PagingData<VideoDocument>> = _videosFlow

    private val _blogsFlow = MutableStateFlow<PagingData<BlogDocument>>(PagingData.empty())
    val blogsFlow: StateFlow<PagingData<BlogDocument>> = _blogsFlow

    private val _imagesFlow = MutableStateFlow<PagingData<ImageDocument>>(PagingData.empty())
    val imagesFlow: StateFlow<PagingData<ImageDocument>> = _imagesFlow

    fun fetchData(query: String) {
        fetchVideos(query)
        fetchBlogs(query)
        fetchImages(query)
    }

    private fun fetchVideos(query: String) {
        viewModelScope.launch {
            repository.getVideos(query).collectLatest {
                _videosFlow.value = it
            }
        }
    }

    private fun fetchBlogs(query: String) {
        viewModelScope.launch {
            repository.getBlogs(query).collectLatest {
                _blogsFlow.value = it
            }
        }
    }

    private fun fetchImages(query: String) {
        viewModelScope.launch {
            repository.getImages(query).collectLatest {
                _imagesFlow.value = it
            }
        }
    }
}