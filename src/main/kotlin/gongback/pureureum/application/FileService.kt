package gongback.pureureum.application

import gongback.pureureum.application.dto.FileInfo
import gongback.pureureum.application.util.NameGenerator
import gongback.pureureum.support.constant.FileType
import org.springframework.stereotype.Service

@Service
class FileService(
    private val storageService: StorageService,
    private val fileNameGenerator: NameGenerator
) {

    fun uploadFile(file: FileInfo, fileType: FileType): String {
        val serverFileName = fileNameGenerator.generate() + "." + getExt(file.originalFileName)
        return storageService.uploadFile(file, fileType, serverFileName)
    }

    fun getFileUrl(fileKey: String): String {
        return storageService.getUrl(fileKey)
    }

    fun deleteFile(fileKey: String) {
        storageService.deleteFile(fileKey)
    }

    fun validateFileName(fileName: String?): String {
        val originalFileName = (fileName ?: throw IllegalArgumentException("원본 파일 이름이 존재하지 않습니다"))
        require(originalFileName.isNotBlank()) { throw IllegalArgumentException("원본 파일 이름이 비어있습니다") }
        return originalFileName
    }

    fun getImageType(fileContentType: String?): String {
        val contentType = fileContentType ?: throw IllegalArgumentException("파일 형식이 유효하지 않습니다")
        validateImageType(contentType)
        return contentType
    }

    fun getAnyContentType(fileContentType: String?): String {
        return fileContentType ?: throw IllegalArgumentException("파일 형식이 유효하지 않습니다")
    }

    private fun getExt(fileName: String): String {
        return fileName.substring(fileName.lastIndexOf(".") + 1)
    }

    private fun validateImageType(contentType: String) {
        require(contentType.startsWith("image")) { "이미지 형식의 파일만 가능합니다" }
    }
}
