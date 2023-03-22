package gongback.pureureum.domain.user

import jakarta.persistence.AttributeOverride
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDate

@Entity
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Embedded
    var information: UserInformation,

    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "password", nullable = false))
    var password: Password
) {
    val email: String
        get() = information.email

    val phoneNumber: String
        get() = information.phoneNumber

    val name: String
        get() = information.name

    val gender: Gender
        get() = information.gender

    val birthday: LocalDate
        get() = information.birthday

    val role: Role
        get() = information.role

    constructor(
        email: String,
        phoneNumber: String,
        name: String,
        gender: Gender,
        birthday: LocalDate,
        password: Password,
        role: Role,
        id: Long = 0L
    ) : this(
        id,
        UserInformation(name, email, phoneNumber, gender, birthday, role = role),
        password
    )

    fun authenticate(password: Password) {
        identify(this.password == password) { "비밀번호가 일치하지 않습니다" }
    }

    private fun identify(value: Boolean, message: () -> Any = {}) {
        if (!value) {
            throw IllegalArgumentException(message().toString())
        }
    }
}
