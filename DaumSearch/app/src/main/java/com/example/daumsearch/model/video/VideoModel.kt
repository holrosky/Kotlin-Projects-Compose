package com.example.daumsearch.model.video

import com.example.daumsearch.model.Meta
import com.example.daumsearch.model.Model

data class VideoModel(
    val documents: List<VideoDocument>,
    val meta: Meta
) : Model()