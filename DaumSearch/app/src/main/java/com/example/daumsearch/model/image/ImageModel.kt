package com.example.daumsearch.model.image

import com.example.daumsearch.model.Meta
import com.example.daumsearch.model.Model

data class ImageModel(
    val documents: List<ImageDocument>,
    val meta: Meta
) : Model()