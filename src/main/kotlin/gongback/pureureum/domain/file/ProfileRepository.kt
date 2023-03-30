package gongback.pureureum.domain.file

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

fun ProfileRepository.getFileKey(id: Long): String {
    return findFileKeyById(id) ?: throw IllegalArgumentException("프로필 이미지 정보가 존재하지 않습니다")
}

interface ProfileRepository : JpaRepository<Profile, Long> {
    @Query("select p.fileKey from Profile p where p.id = :id")
    fun findFileKeyById(id: Long): String?
}
