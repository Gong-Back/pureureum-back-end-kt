package gongback.pureureum.application

import gongback.pureureum.application.dto.FileDto
import gongback.pureureum.domain.file.FileType
import gongback.pureureum.domain.file.Profile
import gongback.pureureum.domain.file.ProfileRepository
import gongback.pureureum.domain.file.getFileKey
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

private const val IMAGE_TYPE = "image"

@Service
@Transactional(readOnly = true)
class ProfileService(
    private val uploadService: UploadService,
    private val profileRepository: ProfileRepository
) : FileService {

    override fun uploadFile(file: MultipartFile, fileType: FileType): FileDto {
        val originalFileName = validateFileName(file)
        val contentType = validateContentType(file)
        val serverFileName = uploadService.createServerFileName(originalFileName)
        val fileKey = uploadService.uploadFile(file, fileType, serverFileName)
        return FileDto(fileKey, contentType, originalFileName, serverFileName)
    }

    override fun getFileUrl(id: Long): String {
        val fileKey = profileRepository.getFileKey(id)
        return uploadService.getFileUrl(fileKey)
    }

    fun save(file: Profile) {
        profileRepository.save(file)
    }

    fun deleteFile(id: Long, fileKey: String) {
        profileRepository.deleteById(id)
        uploadService.deleteFile(fileKey)
    }

    fun getProfile(id: Long): Profile {
        return profileRepository.getReferenceById(id)
    }

    private fun validateFileName(file: MultipartFile): String {
        val originalFileName = (
            file.originalFilename
                ?: throw IllegalArgumentException("원본 파일 이름이 존재하지 않습니다")
            )
        require(originalFileName.isNotBlank()) { throw IllegalArgumentException("원본 파일 이름이 비어있습니다") }
        return originalFileName
    }

    private fun validateContentType(file: MultipartFile): String {
        val contentType = file.contentType
            ?: throw IllegalArgumentException("파일 형식이 유효하지 않습니다")
        require(contentType.startsWith(IMAGE_TYPE)) { "이미지 형식의 파일만 가능합니다" }
        return contentType
    }
}
