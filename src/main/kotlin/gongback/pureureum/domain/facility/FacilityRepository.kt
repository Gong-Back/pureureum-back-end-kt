package gongback.pureureum.domain.facility

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

fun FacilityRepository.getApprovedByCategory(category: FacilityCategory, userId: Long): List<Facility> {
    return findByCategoryAndUserIdAndProgress(category, userId, FacilityProgress.APPROVED)
}

fun FacilityRepository.getByUserId(userId: Long): List<Facility> {
    return findByUserId(userId)
}

fun FacilityRepository.getDocFileKeyByDocId(docId: Long): String {
    return findDocFileKeyByDocId(docId) ?: throw IllegalArgumentException("존재하지 않는 파일입니다")
}

interface FacilityRepository : JpaRepository<Facility, Long> {
    fun findByCategoryAndUserIdAndProgress(category: FacilityCategory, userId: Long, progress: FacilityProgress): List<Facility>
    fun findByUserId(userId: Long): List<Facility>

    @Query("select fc.fileKey from FacilityCertificationDoc fc where fc.id=:docId")
    fun findDocFileKeyByDocId(@Param("docId") docId: Long): String?
}
