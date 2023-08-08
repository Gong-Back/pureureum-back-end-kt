package gongback.pureureum.application

import gongback.pureureum.application.dto.FacilityReq
import gongback.pureureum.application.dto.FacilityRes
import gongback.pureureum.application.dto.FacilityResWithProgress
import gongback.pureureum.application.dto.FacilityWithDocIds
import gongback.pureureum.domain.facility.FacilityCertificationDoc
import gongback.pureureum.domain.facility.FacilityProgress
import gongback.pureureum.domain.facility.FacilityRepository
import gongback.pureureum.domain.facility.getAllNotApprovedByCategory
import gongback.pureureum.domain.facility.getApprovedByCategoryAndUserId
import gongback.pureureum.domain.facility.getByUserId
import gongback.pureureum.domain.facility.getDocFileKeyByDocId
import gongback.pureureum.domain.facility.getFacilityById
import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.getUserByEmail
import gongback.pureureum.support.constant.Category
import gongback.pureureum.support.constant.FileType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional(readOnly = true)
class FacilityService(
    private val facilityRepository: FacilityRepository,
    private val userRepository: UserRepository,
    private val fileService: FileService
) {

    @Transactional
    fun registerFacility(userEmail: String, facilityReq: FacilityReq, certificationDoc: List<MultipartFile>?) {
        val user = userRepository.getUserByEmail(userEmail)
        val facility = facilityReq.toFacility(user.id)

        certificationDoc?.let {
            it.forEach { file ->
                val originalFileName = fileService.validateFileName(file)
                val contentType = fileService.getImageType(file)
                val fileKey = fileService.uploadFile(file, FileType.FACILITY_CERTIFICATION, originalFileName)
                val facilityCertificationDoc = FacilityCertificationDoc(fileKey, contentType, originalFileName)
                facility.addCertificationDoc(facilityCertificationDoc)
            }
        }
        facilityRepository.save(facility)
    }

    fun getApprovedFacilityByCategory(userEmail: String, category: Category): List<FacilityRes> {
        val user = userRepository.getUserByEmail(userEmail)
        val facilities = facilityRepository.getApprovedByCategoryAndUserId(category, user.id)
        return facilities.map {
            FacilityRes.fromFacility(it)
        }
    }

    fun getAllFacilities(userEmail: String): List<FacilityResWithProgress> {
        val user = userRepository.getUserByEmail(userEmail)
        val facilities = facilityRepository.getByUserId(user.id)
        return facilities.map {
            FacilityResWithProgress.fromFacility(it)
        }
    }

    fun getCertificationDocDownloadPath(id: Long, docId: Long): String {
        val fileKey = facilityRepository.getDocFileKeyByDocId(id, docId)
        return fileService.getFileUrl(fileKey)
    }

    fun getNotApprovedFacilitiesByCategory(category: Category): List<FacilityRes> {
        val facilities = facilityRepository.getAllNotApprovedByCategory(category)
        return facilities.map {
            FacilityRes.fromFacility(it)
        }
    }

    fun getFacilityById(id: Long): FacilityWithDocIds {
        val facility = facilityRepository.getFacilityById(id)
        val docIds = facility.certificationDoc.map {
            it.id
        }
        return facility.let {
            FacilityWithDocIds.fromFacility(it, docIds)
        }
    }

    @Transactional
    fun updateFacilityProgress(id: Long, progress: FacilityProgress) {
        val facility = facilityRepository.getFacilityById(id)
        facility.updateProgress(progress)
    }

    @Transactional
    fun updateFacilitiesProgress(ids: List<Long>, progress: FacilityProgress) {
        facilityRepository.updateProgressByIds(ids, progress)
    }
}
