package gongback.pureureum.application

import gongback.pureureum.support.constant.FileType
import org.springframework.web.multipart.MultipartFile

interface StorageService {
    fun uploadFile(image: MultipartFile, fileType: FileType, serverFileName: String): String
    fun getUrl(fileKey: String): String
    fun deleteFile(fileKey: String)
}
