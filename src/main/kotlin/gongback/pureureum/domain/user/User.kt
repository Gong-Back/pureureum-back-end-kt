package gongback.pureureum.domain.user

import gongback.pureureum.domain.file.Profile
import jakarta.persistence.AttributeOverride
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
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
    var password: Password,

    @Enumerated(EnumType.STRING)
    val socialType: SocialType,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinColumn(name = "profile_id")
    var profile: Profile
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

    val nickname: String
        get() = information.nickname

    constructor(
        email: String,
        phoneNumber: String,
        name: String,
        nickname: String,
        gender: Gender,
        birthday: LocalDate,
        password: Password,
        role: Role,
        socialType: SocialType,
        profile: Profile,
        id: Long = 0L
    ) : this(
        id,
        UserInformation(name, email, phoneNumber, nickname, gender, birthday, role = role),
        password,
        socialType,
        profile
    )

    fun authenticate(password: Password) {
        identify(this.password == password) { "비밀번호가 일치하지 않습니다" }
    }

    fun updatePhoneNumber(phoneNumber: String) {
        information = information.copy(phoneNumber = phoneNumber)
    }

    fun updatePassword(password: Password) {
        this.password = password
    }

    fun updateNickname(nickname: String) {
        information = information.copy(nickname = nickname)
    }

    private fun identify(value: Boolean, message: () -> Any = {}) {
        if (!value) {
            throw IllegalArgumentException(message().toString())
        }
    }
}
