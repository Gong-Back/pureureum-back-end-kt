package gongback.pureureum.domain.facility

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

fun FacilityRepository.getApprovedByCategoryAndUserId(category: FacilityCategory, userId: Long): List<Facility> {
    return findByCategoryAndUserIdAndProgress(category, userId, FacilityProgress.APPROVED)
}

fun FacilityRepository.getByUserId(userId: Long): List<Facility> {
    return findByUserId(userId)
}

fun FacilityRepository.getDocFileKeyByDocId(facilityId: Long, docId: Long): String {
    return findDocFileKeyByDocId(facilityId, docId) ?: throw IllegalArgumentException("존재하지 않는 파일입니다")
}

fun FacilityRepository.getAllNotApprovedByCategory(category: FacilityCategory): List<Facility> {
    return findByCategoryAndProgress(category, FacilityProgress.NOT_APPROVED)
}

fun FacilityRepository.getFacilityById(id: Long): Facility {
    return findFacilityById(id) ?: throw IllegalArgumentException("시설 정보가 존재하지 않습니다")
}

interface FacilityRepository : JpaRepository<Facility, Long> {

    fun findByCategoryAndUserIdAndProgress(category: FacilityCategory, userId: Long, progress: FacilityProgress): List<Facility>

    fun findByUserId(userId: Long): List<Facility>

    @Query("select fc.fileKey from FacilityCertificationDoc fc join Facility f where fc.id=:docId and f.id=:facilityId")
    fun findDocFileKeyByDocId(@Param("facilityId") facilityId: Long, @Param("docId") docId: Long): String?
    fun findByCategoryAndProgress(category: FacilityCategory, progress: FacilityProgress): List<Facility>
    fun findFacilityById(id: Long): Facility?

    @Modifying
    @Query("update Facility f set f.progress=:progress where f.id in :ids")
    fun updateProgressByIds(@Param("ids") ids: List<Long>, @Param("progress") progress: FacilityProgress)
}
