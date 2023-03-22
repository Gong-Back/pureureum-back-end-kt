package gongback.pureureum.domain.user

import org.springframework.data.jpa.repository.JpaRepository

fun UserRepository.existsByEmail(email: String): Boolean = existsByInformationEmail(email)

fun UserRepository.existsByPhoneNumber(phoneNumber: String): Boolean = existsByInformationPhoneNumber(phoneNumber)

fun UserRepository.getUserByEmail(email: String): User =
    findByInformationEmail(email) ?: throw IllegalArgumentException("요청하신 사용자 정보를 찾을 수 없습니다")

interface UserRepository : JpaRepository<User, Long> {
    fun existsByInformationEmail(email: String): Boolean

    fun existsByInformationPhoneNumber(phoneNumber: String): Boolean

    fun findByInformationEmail(email: String): User?
}
