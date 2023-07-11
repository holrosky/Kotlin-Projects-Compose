package com.example.kakaoexam.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.daumsearch.api.ApiService
import com.example.daumsearch.model.blog.BlogDocument
import com.example.daumsearch.model.blog.BlogModel
import com.example.daumsearch.model.image.ImageDocument
import com.example.daumsearch.model.image.ImageModel
import com.example.daumsearch.model.video.VideoDocument
import com.example.daumsearch.model.video.VideoModel
import com.example.daumsearch.paging.GenericPagingSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class Repository @Inject constructor(
    private val apiService: ApiService,
    private val coroutineScope: CoroutineScope
) {
    fun getVideos(query: String): Flow<PagingData<VideoDocument>> {
        return Pager(
            config = PagingConfig(pageSize = 10),
            pagingSourceFactory = { GenericPagingSource<VideoDocument, VideoModel>(apiService, query) { query, page ->
                getVideo(query, page)
            } }
        ).flow.cachedIn(coroutineScope)
    }

    fun getBlogs(query: String): Flow<PagingData<BlogDocument>> {
        return Pager(
            config = PagingConfig(pageSize = 10),
            pagingSourceFactory = { GenericPagingSource<BlogDocument, BlogModel>(apiService, query) { query, page ->
                getBlog(query, page)
            } }
        ).flow.cachedIn(coroutineScope)
    }

    fun getImages(query: String): Flow<PagingData<ImageDocument>> {
        return Pager(
            config = PagingConfig(pageSize = 10),
            pagingSourceFactory = { GenericPagingSource<ImageDocument, ImageModel>(apiService, query) { query, page ->
                getImage(query, page)
            } }
        ).flow.cachedIn(coroutineScope)
    }
}