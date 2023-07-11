package com.example.daumsearch.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.daumsearch.api.ApiService
import com.example.daumsearch.model.Document
import com.example.daumsearch.model.Model
import com.example.daumsearch.model.blog.BlogModel
import com.example.daumsearch.model.image.ImageModel
import com.example.daumsearch.model.video.VideoModel
import retrofit2.Response
import java.io.IOException

class GenericPagingSource <T : Document, K: Model>(
    private val apiService: ApiService,
    private val query: String,
    private val fetch: suspend ApiService.(String?, Int?) -> Response<K>
) : PagingSource<Int, T>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        try {
            val nextPageNumber = params.key ?: 1
            val response = apiService.fetch(query, nextPageNumber)
            println(response.body())
            if (response.isSuccessful) {
                val model = response.body()

                @Suppress("UNCHECKED_CAST")
                val data = when (model) {
                    is VideoModel -> model.documents as List<T>
                    is BlogModel -> model.documents as List<T>
                    is ImageModel -> model.documents as List<T>
                    else -> emptyList()
                }

                val prevKey = if (nextPageNumber == 1) null else nextPageNumber - 1
                val nextKey = if (data.isNotEmpty()) nextPageNumber + 1 else null

                return LoadResult.Page(
                    data = data,
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            } else {
                return LoadResult.Error(IOException("Failed to fetch data"))
            }
        } catch (e: Exception) {
            // Handle error
            for (each in e.stackTrace) {
                println(each.toString())
            }

            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        return state.anchorPosition
    }
}