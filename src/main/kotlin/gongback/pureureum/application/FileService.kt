package gongback.pureureum.application

import gongback.pureureum.application.dto.FileDto
import gongback.pureureum.domain.file.FileType
import org.springframework.web.multipart.MultipartFile

interface FileService {
    fun uploadFile(file: MultipartFile, fileType: FileType): FileDto
    fun getFileUrl(id: Long): String
}
