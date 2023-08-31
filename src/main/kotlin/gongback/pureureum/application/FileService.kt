package gongback.pureureum.application

import gongback.pureureum.application.dto.FileDto
import gongback.pureureum.application.util.NameGenerator
import gongback.pureureum.support.constant.FileType
import org.springframework.stereotype.Service

private const val FILE_NAME_FORMAT = "%s.%s"

@Service
class FileService(
    private val storageService: StorageService,
    private val fileNameGenerator: NameGenerator
) {

    fun uploadFile(file: FileDto, fileType: FileType): String {
        val serverFileName = generateServerFileName(file.originalFileName)
        return storageService.uploadFile(file, fileType, serverFileName)
    }

    fun getFileUrl(fileKey: String): String =
        storageService.getUrl(fileKey)

    fun deleteFile(fileKey: String) =
        storageService.deleteFile(fileKey)

    fun validateFileName(fileName: String?): String =
        fileName.apply {
            require(!fileName.isNullOrBlank()) {
                "원본 파일 이름이 비어있습니다"
            }
        }!!

    fun validateImageType(fileContentType: String?): String =
        fileContentType.apply {
            require(fileContentType != null) {
                "파일 형식이 유효하지 않습니다"
            }
            validateImageType(fileContentType)
        }!!

    fun validateAnyContentType(fileContentType: String?): String =
        fileContentType.apply {
            require(fileContentType != null) {
                "파일 형식이 유효하지 않습니다"
            }
        }!!

    private fun generateServerFileName(originalFileName: String): String =
        String.format(FILE_NAME_FORMAT, fileNameGenerator.generate(), getExt(originalFileName))

    private fun getExt(fileName: String): String =
        fileName.substring(fileName.lastIndexOf(".") + 1)

    private fun validateImageType(contentType: String) =
        require(contentType.startsWith("image")) { "이미지 형식의 파일만 가능합니다" }
}
