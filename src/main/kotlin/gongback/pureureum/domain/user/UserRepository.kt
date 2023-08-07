package gongback.pureureum.domain.user

import org.springframework.data.jpa.repository.JpaRepository

fun UserRepository.existsEmailOrNickname(email: String): Boolean =
    existsByInformationEmailOrInformationNickname(email, email)

fun UserRepository.existsByPhoneNumber(phoneNumber: String): Boolean = existsByInformationPhoneNumber(phoneNumber)

fun UserRepository.getUserByEmail(email: String): User =
    findByInformationEmail(email) ?: throw NoSuchElementException("요청하신 사용자 정보를 찾을 수 없습니다")

fun UserRepository.existsEmail(email: String): Boolean = existsByInformationEmail(email)

fun UserRepository.getUserByPhoneNumber(email: String): User =
    findByInformationPhoneNumber(email) ?: throw NoSuchElementException("요청하신 사용자 정보를 찾을 수 없습니다")

fun UserRepository.existsNickname(nickname: String): Boolean = existsByInformationNickname(nickname)

interface UserRepository : JpaRepository<User, Long> {
    fun existsByInformationEmailOrInformationNickname(email: String, nickname: String): Boolean

    fun existsByInformationPhoneNumber(phoneNumber: String): Boolean

    fun findByInformationEmail(email: String): User?

    fun findByInformationPhoneNumber(phoneNumber: String): User?

    fun existsByInformationEmail(email: String): Boolean

    fun existsByInformationNickname(nickname: String): Boolean
}
