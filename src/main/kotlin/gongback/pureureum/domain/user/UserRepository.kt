package gongback.pureureum.domain.user

import org.springframework.data.jpa.repository.JpaRepository

fun UserRepository.existsByEmail(email: String): Boolean = existsByInformationEmail(email)

fun UserRepository.existsByPhoneNumber(phoneNumber: String): Boolean = existsByInformationPhoneNumber(phoneNumber)

interface UserRepository : JpaRepository<User, Long> {
    fun existsByInformationEmail(email: String): Boolean

    fun existsByInformationPhoneNumber(phoneNumber: String): Boolean
}
