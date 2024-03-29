package gongback.pureureum.domain.user

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.time.LocalDate

@Embeddable
data class UserInformation(
    @Column(nullable = false, length = 100)
    val name: String,

    @Column(unique = true, nullable = false, length = 30)
    val email: String,

    @Column(unique = true, nullable = false, length = 13)
    var phoneNumber: String,

    @Column(unique = true, nullable = false, length = 30)
    var nickname: String,

    @Column(nullable = false, length = 6)
    @Enumerated(EnumType.STRING)
    val gender: UserGender,

    @Column(nullable = false)
    val birthday: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    val userRole: UserRole
)
