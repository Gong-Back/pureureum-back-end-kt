package gongback.pureureum.application.dto

import java.io.InputStream

data class FileInfo(
    val size: Long,
    val inputStream: InputStream,
    val contentType: String,
    val originalFileName: String
)
