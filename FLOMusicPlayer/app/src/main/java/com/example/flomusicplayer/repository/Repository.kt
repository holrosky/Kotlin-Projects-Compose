package com.example.flomusicplayer.repository

import com.example.flomusicplayer.api.ApiService
import javax.inject.Inject

class Repository @Inject constructor(private val apiService: ApiService) {

    suspend fun getMusic() = apiService.getMusic()

    suspend fun getMusicList() = apiService.getMusicList()
}