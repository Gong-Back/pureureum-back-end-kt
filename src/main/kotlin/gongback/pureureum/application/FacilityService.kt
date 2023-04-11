package gongback.pureureum.application

import gongback.pureureum.application.dto.ErrorCode
import gongback.pureureum.application.dto.FacilityReq
import gongback.pureureum.application.dto.FacilityRes
import gongback.pureureum.application.dto.FacilityResWithProgress
import gongback.pureureum.domain.facility.FacilityCategory
import gongback.pureureum.domain.facility.FacilityCertificationDoc
import gongback.pureureum.domain.facility.FacilityRepository
import gongback.pureureum.domain.facility.getApprovedByCategory
import gongback.pureureum.domain.facility.getByUserId
import gongback.pureureum.domain.facility.getDocFileKeyByDocId
import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.getUserByEmail
import gongback.pureureum.support.enum.FileType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional(readOnly = true)
class FacilityService(
    private val facilityRepository: FacilityRepository,
    private val userRepository: UserRepository,
    private val uploadService: UploadService
) {

    @Transactional
    fun registerFacility(userEmail: String, facilityReq: FacilityReq, certificationDoc: List<MultipartFile>?) {
        val user = userRepository.getUserByEmail(userEmail)
        val facility = facilityReq.toFacility(user.id)

        certificationDoc?.let {
            it.forEach { file ->
                val originalFileName = validateFileName(file)
                val contentType = validateContentType(file)
                val fileKey = uploadService.uploadFile(file, FileType.FACILITY_CERTIFICATION, originalFileName)
                val facilityCertificationDoc = FacilityCertificationDoc(fileKey, contentType, originalFileName)
                facility.addCertificationDoc(facilityCertificationDoc)
            }
        }
        facilityRepository.save(facility)
    }

    fun getFacilityByCategory(userEmail: String, category: String): List<FacilityRes> {
        val facilityCategory = validateCategory(category)
        val user = userRepository.getUserByEmail(userEmail)
        val facilities = facilityRepository.getApprovedByCategory(facilityCategory, user.id)
        return facilities.map {
            FacilityRes.fromFacility(it)
        }
    }

    fun getFacilities(userEmail: String): List<FacilityResWithProgress> {
        val user = userRepository.getUserByEmail(userEmail)
        val facilities = facilityRepository.getByUserId(user.id)
        return facilities.map {
            FacilityResWithProgress.fromFacility(it)
        }
    }

    fun getCertificationDocDownloadPath(docId: Long): String {
        val fileKey = facilityRepository.getDocFileKeyByDocId(docId)
        return uploadService.getFileUrl(fileKey)
    }

    private fun validateCategory(category: String): FacilityCategory {
        return try {
            FacilityCategory.valueOf(category)
        } catch (e: IllegalArgumentException) {
            throw PureureumException(errorCode = ErrorCode.ENUM_VALUE_INVALID)
        }
    }

    private fun validateFileName(file: MultipartFile): String {
        val originalFileName = (file.originalFilename ?: throw IllegalArgumentException("원본 파일 이름이 존재하지 않습니다"))
        require(originalFileName.isNotBlank()) { throw IllegalArgumentException("원본 파일 이름이 비어있습니다") }
        return originalFileName
    }

    private fun validateContentType(file: MultipartFile): String {
        return file.contentType ?: throw IllegalArgumentException("파일 형식이 유효하지 않습니다")
    }
}
