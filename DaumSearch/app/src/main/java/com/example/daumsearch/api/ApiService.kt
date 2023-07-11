package com.example.daumsearch.api

import com.example.daumsearch.BuildConfig
import com.example.daumsearch.model.blog.BlogModel
import com.example.daumsearch.model.image.ImageModel

import com.example.daumsearch.model.video.VideoModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiService {
    @GET("vclip")
    @Headers("Authorization: KakaoAK ${BuildConfig.KAKAO_API_KEY}")
    suspend fun getVideo(
        @Query("query") query: String?,
        @Query("page") page: Int?
    ): Response<VideoModel>

    @GET("image")
    @Headers("Authorization: KakaoAK ${BuildConfig.KAKAO_API_KEY}")
    suspend fun getImage(
        @Query("query") query: String?,
        @Query("page") page: Int?
    ): Response<ImageModel>

    @GET("blog")
    @Headers("Authorization: KakaoAK ${BuildConfig.KAKAO_API_KEY}")
    suspend fun getBlog(
        @Query("query") query: String?,
        @Query("page") page: Int?
    ): Response<BlogModel>
}