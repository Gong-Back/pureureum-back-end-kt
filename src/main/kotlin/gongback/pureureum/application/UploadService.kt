package gongback.pureureum.application

import gongback.pureureum.application.util.NameGenerator
import gongback.pureureum.support.enum.FileType
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class UploadService(
    private val storageService: StorageService,
    private val fileNameGenerator: NameGenerator
) {

    fun uploadFile(file: MultipartFile, fileType: FileType, originalFileName: String): String {
        val serverFileName = fileNameGenerator.generate() + "." + getExt(originalFileName)
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

    fun validateFileName(file: MultipartFile): String {
        val originalFileName = (file.originalFilename ?: throw IllegalArgumentException("원본 파일 이름이 존재하지 않습니다"))
        require(originalFileName.isNotBlank()) { throw IllegalArgumentException("원본 파일 이름이 비어있습니다") }
        return originalFileName
    }

    fun validateContentType(file: MultipartFile): String {
        val contentType = file.contentType
            ?: throw IllegalArgumentException("파일 형식이 유효하지 않습니다")
        require(contentType.startsWith("image")) { "이미지 형식의 파일만 가능합니다" }
        return contentType
    }
}
