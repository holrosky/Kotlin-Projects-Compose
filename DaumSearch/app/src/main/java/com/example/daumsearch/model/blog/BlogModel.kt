package com.example.daumsearch.model.blog

import com.example.daumsearch.model.Meta
import com.example.daumsearch.model.Model

data class BlogModel(
    val documents: List<BlogDocument>,
    val meta: Meta
) : Model()