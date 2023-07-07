package com.example.flomusicplayer.api

import com.example.flomusicplayer.model.MusicModel
import retrofit2.http.GET

interface ApiService {
    @GET("song.json")
    suspend fun getMusic(): MusicModel

    @GET("song.json")
    suspend fun getMusicList(): List<MusicModel>
}