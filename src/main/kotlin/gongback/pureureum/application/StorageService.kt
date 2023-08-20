package gongback.pureureum.application

import gongback.pureureum.application.dto.FileInfo
import gongback.pureureum.support.constant.FileType

interface StorageService {
    fun uploadFile(image: FileInfo, fileType: FileType, serverFileName: String): String
    fun getUrl(fileKey: String): String
    fun deleteFile(fileKey: String)
}
