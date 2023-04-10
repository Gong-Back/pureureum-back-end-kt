package gongback.pureureum.application.dto

import gongback.pureureum.domain.user.Profile

data class FileDto(
    val fileKey: String,
    val contentType: String,
    val originalFileName: String
) {
    fun toProfile(): Profile {
        return Profile(
            fileKey = fileKey,
            contentType = contentType,
            originalFileName = originalFileName
        )
    }
}

data class FileRes(
    val fileUrl: String
)
