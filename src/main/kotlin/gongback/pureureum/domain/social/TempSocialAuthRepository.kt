package gongback.pureureum.domain.social

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying

fun TempSocialAuthRepository.getTempByEmail(email: String): TempSocialAuth =
    findByEmail(email) ?: throw NoSuchElementException("요청하신 임시 소셜 사용자 정보를 찾을 수 없습니다")

interface TempSocialAuthRepository : JpaRepository<TempSocialAuth, Long> {
    fun findByEmail(email: String): TempSocialAuth?

    fun existsByEmail(email: String): Boolean

    @Modifying
    fun deleteByEmail(email: String)
}
