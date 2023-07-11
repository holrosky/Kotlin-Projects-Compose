package com.example.daumsearch.model.image

import com.example.daumsearch.model.Document

data class ImageDocument(
    val collection: String,
    val datetime: String,
    val display_sitename: String,
    val doc_url: String,
    val height: Int,
    val image_url: String,
    val thumbnail_url: String,
    val width: Int
) : Document()