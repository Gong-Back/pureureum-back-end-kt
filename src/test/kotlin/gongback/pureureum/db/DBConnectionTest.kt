package gongback.pureureum.db

import gongback.pureureum.domain.user.User
import gongback.pureureum.domain.user.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("local")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DBConnectionTest @Autowired constructor(val userRepository: UserRepository) {
    @Test
    fun `DB 연결 테스트`() {
        // given
        val userName = "testUser"
        val userEmail = "test@test.com"
        val testUser = User(name = userName, email = userEmail)

        // when
        val savedUser = userRepository.save(testUser)

        // then
        assertEquals(userName, savedUser.name)
    }
}
