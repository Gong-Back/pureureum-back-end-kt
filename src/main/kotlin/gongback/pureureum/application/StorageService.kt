package gongback.pureureum.application

import gongback.pureureum.domain.file.FileType
import org.springframework.web.multipart.MultipartFile

interface StorageService {
    fun uploadFile(image: MultipartFile, type: FileType, serverFileName: String): String
    fun getUrl(fileKey: String): String
    fun deleteFile(fileKey: String)
}
