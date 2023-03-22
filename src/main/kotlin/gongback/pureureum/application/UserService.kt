package gongback.pureureum.application

import gongback.pureureum.domain.user.User
import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.getUserByEmail
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository
) {
    fun getUserByEmail(email: String): User = userRepository.getUserByEmail(email)
}
