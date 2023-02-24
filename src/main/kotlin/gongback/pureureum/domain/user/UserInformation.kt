package gongback.pureureum.domain.user

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class UserInformation(
    @Column(nullable = false, length = 30)
    val name: String,
    @Column(unique = true, nullable = false)
    val email: String,
) {
}