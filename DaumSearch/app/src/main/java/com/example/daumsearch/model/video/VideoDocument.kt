package com.example.daumsearch.model.video

import com.example.daumsearch.model.Document

data class VideoDocument(
    val author: String,
    val datetime: String,
    val play_time: Int,
    val thumbnail: String,
    val title: String,
    val url: String
) : Document()