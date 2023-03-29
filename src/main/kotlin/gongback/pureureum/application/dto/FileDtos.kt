package gongback.pureureum.application.dto

import gongback.pureureum.domain.file.Profile

data class FileDto(
    val fileKey: String,
    val contentType: String,
    val originalFileName: String,
    val serverFileName: String
) {
    fun toProfile(): Profile {
        return Profile(fileKey, contentType, originalFileName, serverFileName)
    }
}

data class FileRes(
    val fileUrl: String
)
