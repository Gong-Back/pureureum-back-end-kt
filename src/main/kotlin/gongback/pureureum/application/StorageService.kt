package gongback.pureureum.application

import gongback.pureureum.application.dto.FileDto
import gongback.pureureum.support.constant.FileType

interface StorageService {
    fun uploadFile(image: FileDto, fileType: FileType, serverFileName: String): String
    fun getUrl(fileKey: String): String
    fun deleteFile(fileKey: String)
}
