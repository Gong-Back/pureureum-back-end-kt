package gongback.pureureum.domain.user

import org.springframework.data.jpa.repository.JpaRepository

fun UserRepository.existsByEmail(email: String): Boolean = existsByInformationEmail(email)

interface UserRepository : JpaRepository<User, Long> {
    fun existsByInformationEmail(email: String): Boolean
}
