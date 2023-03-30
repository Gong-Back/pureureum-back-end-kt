package gongback.pureureum.application

import gongback.pureureum.application.util.NameGenerator
import gongback.pureureum.domain.file.FileType
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class UploadService(
    private val storageService: StorageService,
    private val fileNameGenerator: NameGenerator
) {
    fun createServerFileName(originalFileName: String): String {
        return fileNameGenerator.generate() + "." + getExt(originalFileName)
    }

    fun uploadFile(file: MultipartFile, fileType: FileType, serverFileName: String): String {
        return storageService.uploadFile(file, fileType, serverFileName)
    }

    fun getFileUrl(fileKey: String): String {
        return storageService.getUrl(fileKey)
    }

    fun deleteFile(fileKey: String) {
        storageService.deleteFile(fileKey)
    }

    private fun getExt(fileName: String): String {
        return fileName.substring(fileName.lastIndexOf(".") + 1)
    }
}
