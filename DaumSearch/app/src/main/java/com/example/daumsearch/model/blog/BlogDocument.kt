package com.example.daumsearch.model.blog

import com.example.daumsearch.model.Document

data class BlogDocument(
    val blogname: String,
    val contents: String,
    val datetime: String,
    val thumbnail: String,
    val title: String,
    val url: String
) : Document()