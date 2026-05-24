package com.auralyx.domain.model
data class Album(val id: Long, val name: String, val artist: String, val artUri: String?, val songCount: Int, val year: Int = 0)
