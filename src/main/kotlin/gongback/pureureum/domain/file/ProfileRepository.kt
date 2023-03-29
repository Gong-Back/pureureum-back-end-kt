package gongback.pureureum.domain.file

import org.springframework.data.jpa.repository.JpaRepository

fun ProfileRepository.getFileKey(id: Long): String {
    val profile = findById(id).orElseThrow { IllegalArgumentException("프로필 이미지 정보가 존재하지 않습니다") }
    return profile.fileKey
}

interface ProfileRepository : JpaRepository<Profile, Long>
