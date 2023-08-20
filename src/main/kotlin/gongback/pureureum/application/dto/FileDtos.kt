package gongback.pureureum.application.dto

import java.io.InputStream

data class FileReq(
    val size: Long,
    val inputStream: InputStream,
    val contentType: String?,
    val originalFileName: String?
) {
    fun toFileInfo(contentType: String, originalFileName: String): FileInfo = FileInfo(
        size,
        inputStream,
        contentType,
        originalFileName
    )
}

data class FileInfo(
    val size: Long,
    val inputStream: InputStream,
    val contentType: String,
    val originalFileName: String
)
